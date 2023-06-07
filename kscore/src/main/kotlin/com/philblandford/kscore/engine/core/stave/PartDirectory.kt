package com.philblandford.kscore.engine.core.stave

import com.philblandford.kscore.api.ProgressFunc2
import com.philblandford.kscore.api.noProgress2
import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.PartGeography
import com.philblandford.kscore.engine.core.SystemXGeography
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.util.pFlatMap
import com.philblandford.kscore.log.ksLogt
import com.philblandford.kscore.log.ksLogv
import com.philblandford.kscore.option.getOption

/* create the parts for each system with their individual staves */

data class PartDirectory(private val parts: Map<EventAddress, PartArea>) : PartQuery {
  override fun getParts(): Map<EventAddress, PartArea> {
    return parts
  }
}

internal fun DrawableFactory.partDirectory(
  scoreQuery: ScoreQuery, areaDirectoryQuery: AreaDirectoryQuery,
  geographyXQuery: GeographyXQuery, layoutDescriptor: LayoutDescriptor,
  progress: ProgressFunc2 = noProgress2
): PartDirectory? {

  return createParts(
    scoreQuery, areaDirectoryQuery, geographyXQuery.getSystemXGeographies().toList(),
    layoutDescriptor, progress
  )
}

internal fun PartDirectory.update(
  scoreQuery: ScoreQuery,
  areaDirectoryQuery: AreaDirectoryQuery,
  geographyXQuery: GeographyXQuery,
  layoutDescriptor: LayoutDescriptor,
  full: Boolean,
  barsChanged: Iterable<Int>,
  drawableFactory: DrawableFactory
): PartDirectory {

  val geogs = getGeogs(geographyXQuery, full, barsChanged)

  val newParts = drawableFactory.createParts(scoreQuery, areaDirectoryQuery, geogs, layoutDescriptor)?.
    getParts() ?: mapOf()
  val oldParts = if (full) mapOf() else
    this.getParts().filterNot { part -> barsChanged.any { bar -> part.value.geog.contains(bar) } }
  return PartDirectory(oldParts.plus(newParts))
}

/* No bars have changed, but still need to redraw parts */
internal fun PartDirectory.updateAll(
  scoreQuery: ScoreQuery,
  areaDirectoryQuery: AreaDirectoryQuery,
  geographyXQuery: GeographyXQuery,
  layoutDescriptor: LayoutDescriptor,
  drawableFactory: DrawableFactory
): PartDirectory {


  val newParts = drawableFactory.createParts(scoreQuery, areaDirectoryQuery, geographyXQuery.getSystemXGeographies(), layoutDescriptor)?.
  getParts() ?: mapOf()
  return PartDirectory(newParts)
}

/* Find those geographies affected by a change event */
private fun getGeogs(
  geographyXQuery: GeographyXQuery,
  full: Boolean, barsChanged: Iterable<Int>
): List<SystemXGeography> {
  val geogs = geographyXQuery.getSystemXGeographies()
  return if (full) {
    geogs.toList()
  } else {
    val range = (barsChanged.minOrNull() ?: 0)..(barsChanged.maxOrNull() ?: 0)
    geogs.filter { geog ->
      geog.startBar in range || geog.endBar in range ||
          barsChanged.any { geog.contains(it) }
    }.toList()
  }
}

private fun DrawableFactory.createParts(
  scoreQuery: ScoreQuery, areaDirectoryQuery: AreaDirectoryQuery,
  geogs: List<SystemXGeography>, layoutDescriptor: LayoutDescriptor,
  progress: ProgressFunc2 = noProgress2
): PartDirectory {
  var num = 0
  val partMap = createPartMap(areaDirectoryQuery, geogs, scoreQuery)

  val m =
    geogs.pFlatMap { sysx ->
      num++
      if (progress("System $num bar ${sysx.startBar}", (num.toFloat() / geogs.size) * 100)) {
        ksLogt("Times up!")
        return@pFlatMap listOf()
      }
      val parts = getPartsToDo(sysx.startBar, partMap, scoreQuery)
      parts.mapNotNull { idx ->

        ksLogv("system ${sysx.startBar} part $idx")

        val part = createPartArea(
          partMap,
          areaDirectoryQuery,
          idx,
          scoreQuery.numStaves(idx),
          idx == parts.first(),
          sysx,
          scoreQuery,
          layoutDescriptor,
          EventAddress(sysx.startBar, staveId = StaveId(idx, 0))
        )
        part?.let {
          Pair(ea(sysx.startBar).copy(staveId = StaveId(idx, 0)), it)
        }
      }
    }.toMap()

  return PartDirectory(m.toMap())
}

private fun getPartsToDo(
  sysGeogStart: BarNum,
  partMap: PartMap,
  scoreQuery: ScoreQuery
): Iterable<PartNum> {

  val allParts = scoreQuery.allParts(true)
  if (sysGeogStart == 1 || !getOption<Boolean>(EventParam.OPTION_HIDE_EMPTY_STAVES, scoreQuery)) {
    return allParts
  }

  val parts = scoreQuery.allParts(true).filter { part ->
    (1..scoreQuery.numStaves(part)).any { stave ->
      partMap[Pair(StaveId(part, stave), sysGeogStart)]?.isNotEmpty() ?: false
    }
  }
  return if (parts.isEmpty()) allParts else parts
}

private fun createPartMap(
  areaDirectoryQuery: AreaDirectoryQuery,
  sysGeogs: Iterable<SystemXGeography>,
  scoreQuery: ScoreQuery
): PartMap {

  return scoreQuery.getAllStaves(true).flatMap { staveId ->
    areaDirectoryQuery.getSegmentsForStave(staveId).entries.groupBy { (key, _) ->
      sysGeogs.find { it.contains(key.barNum) }?.startBar ?: 0
    }.map { (bar, list) -> Pair(staveId, bar) to list.associate { Pair(it.key, it.value) } }
  }.toMap()
}

typealias PartMap = Map<Pair<StaveId, BarNum>, SegmentLookup>

private fun SystemXGeography.contains(bar: Int): Boolean {
  return bar in startBar..endBar
}

private fun PartGeography.contains(bar: Int): Boolean {
  return bar in startBar..endBar
}
