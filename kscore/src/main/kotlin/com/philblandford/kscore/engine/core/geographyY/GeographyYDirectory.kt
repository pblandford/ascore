package com.philblandford.kscore.engine.core.geographyY

import com.philblandford.kscore.engine.core.*
import com.philblandford.kscore.engine.core.representation.STAVE_GAP
import com.philblandford.kscore.engine.core.stave.PartArea
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.option.getOption

/* We have all the x-dimensions, and we've created all the staves and parts - from this we
  can work out the dimensions of each system */

data class GeographyYDirectory(
  val systems: Map<Int, SystemYGeography>,
  val pages: Map<Int, PageGeography>
) : GeographyYQuery {
  override fun getSystemYGeographies(): Iterable<SystemYGeography> {
    return systems.values
  }

  override fun getPageGeographies(): Iterable<PageGeography> {
    return pages.values
  }
}

fun geographyYDirectory(
  partQuery: PartQuery,
  geographyXQuery: GeographyXQuery,
  scoreQuery: ScoreQuery
): GeographyYDirectory? {
  val parts = partQuery.getParts()
  val systems = geographyXQuery.getSystemXGeographies().map { it.startBar to it }.toMap()

  val hideIfEmpty = getOption<Boolean>(EventParam.OPTION_HIDE_EMPTY_STAVES, scoreQuery)

  val grouped = parts.toList().groupBy { it.first.barNum }.toSortedMap()
  val sygs = grouped.map {
    it.key to systemYGeography(
      it.value.toMap().toSortedMap(compareBy { it.staveId.main }), systems[it.key]!!,
      hideIfEmpty
    )
  }.toMap()

  val pages = page(1, sygs.values)?.let { mapOf(1 to it) }

  return pages?.let { GeographyYDirectory(sygs, it) }
}

/* Create a geography describing all the y-positions of each part
* The y-margin of the top part becomes the y margin of the system - all y positions are
* relative to this, so the top one is always 0
*/
fun systemYGeography(
  parts: Map<EventAddress, PartArea>,
  systemXGeography: SystemXGeography, hideIfEmpty: Boolean
): SystemYGeography {

  var total = 0

  val reallyHide =
    systemXGeography.startBar != 1 && hideIfEmpty && parts.count { it.value.geog.numSegments != 0 } > 0

  val positions = parts.mapNotNull {
    if (reallyHide && it.value.geog.numSegments == 0) {
      null
    } else {
      val res = addPart(it.value.geog, total)
      total = res.second
      it.key.staveId.main to res.first
    }
  }.toMap().toSortedMap()

  return SystemYGeography(systemXGeography, positions)
}

private fun addPart(geog: PartGeography, total: Int): Pair<PartPosition, Int> {
  // maintain the yMargin for the system, but lower parts are absolute y values
  val yPos = if (total == 0) total else total + geog.yMargin
  var t = total

  if (t == 0) t -= geog.yMargin
  t += geog.height + STAVE_GAP

  return PartPosition(yPos, geog) to t
}

fun page(num:Int, systems: Iterable<SystemYGeography>): PageGeography? {
  return systems.firstOrNull()?.let {
    PageGeography(
      num,
      LayoutDescriptor(),
      sortedMapOf(it.xGeog.startBar to SystemPosition(0, it))
    )
  }
}