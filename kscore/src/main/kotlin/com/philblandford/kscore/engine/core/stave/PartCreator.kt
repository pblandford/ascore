package com.philblandford.kscore.engine.core.stave

import com.philblandford.kscore.engine.core.*
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.barstartend.barLineArea
import com.philblandford.kscore.engine.core.representation.FINAL_BAR_LINE_WIDTH
import com.philblandford.kscore.engine.core.representation.STAVE_HEIGHT
import com.philblandford.kscore.engine.types.*

data class PartArea(val base: Area, val geog: PartGeography)

fun DrawableFactory.createPart(
  partMap: PartMap,
  areaDirectoryQuery: AreaDirectoryQuery,
  mainId: Int,
  numStaves: Int,
  isTop:Boolean,
  systemXGeography: SystemXGeography,
  scoreQuery: ScoreQuery,
  layoutDescriptor: LayoutDescriptor,
  eventAddress: EventAddress
): PartArea? {

  var numSegments = 0

  val staves = (1..numStaves).mapNotNull {
    val segments = partMap[Pair(StaveId(mainId, it), systemXGeography.startBar)] ?: mapOf()

    numSegments += segments.size
    createStave(
      segments, areaDirectoryQuery.getAllHeaderAreas(),
      areaDirectoryQuery.getAllBarStartAreas(),
      areaDirectoryQuery.getAllBarEndAreas(), systemXGeography, scoreQuery,
      eventAddress.copy(staveId = eventAddress.staveId.copy(sub = it)),
      isTop
    )
  }

  var area = Area(
    tag = "Part",
    addressRequirement = AddressRequirement.PART, numBars = systemXGeography.numBars
  )

  staves.withIndex().forEach {
    val address = eventAddress.copy(staveId = StaveId(eventAddress.staveId.main, it.index + 1))
    area = if (it.index == 0) {
      area.addArea(it.value.base, eventAddress = address)
    } else {
      area.addBelow(
        it.value.base, gap = layoutDescriptor.staveGap, eventAddress = address,
        ignoreMargin = true
      )
    }
  }

  val positions = area.childMap.toList().sortedBy { it.first.coord.y }.withIndex().map {
    it.index + 1 to StavePosition(it.value.first.coord.y, staves[it.index].geog)
  }
  val geog = PartGeography(area.height, area.yMargin, positions.toMap().toSortedMap(), numSegments,
    systemXGeography.startBar, systemXGeography.endBar)

  area = addPreHeader(area, geog, eventAddress, areaDirectoryQuery)
  area = addBarLines(area, mainId, systemXGeography, positions, scoreQuery)

  return PartArea(area, geog)
}

private fun addPreHeader(
  area: Area, partGeography: PartGeography,
  eventAddress: EventAddress, areaDirectoryQuery: AreaDirectoryQuery
): Area {
  return areaDirectoryQuery.getAllPreHeaderAreas().get(eventAddress)?.let { pha ->
    areaDirectoryQuery.getAllPreHeaderGeogs().get(eventAddress)?.let { phg ->
      val yPos = partGeography.mainHeight / 2 - pha.base.height / 2
      val copy = area.addArea(pha.base, Coord(phg.joinStart - pha.base.width, yPos), eventAddress)
      copy
    }
  } ?: area
}

private fun DrawableFactory.addBarLines(
  area: Area, part:Int, systemXGeography: SystemXGeography, positions: List<Pair<Int, StavePosition>>,
  scoreQuery: ScoreQuery
): Area {
  var areaCopy = area
  val start = positions.first().second.pos
  val end = positions.last().second.pos
  val height = end + STAVE_HEIGHT - start

  for (pos in systemXGeography.barPositions) {
    areaCopy =
      paintStartBarLine(pos.key, pos.value, start, height,part, scoreQuery, systemXGeography, areaCopy)
    areaCopy =
      paintEndBarLine(pos.key, pos.value, start, height,part, scoreQuery, systemXGeography, areaCopy)
  }
  return areaCopy
}

private fun DrawableFactory.paintStartBarLine(
  barNum: Int, pos: BarPosition, start: Int, height: Int, part:Int,
  scoreQuery: ScoreQuery, systemXGeography: SystemXGeography, area: Area
): Area {
  return scoreQuery.getEvent(EventType.REPEAT_START, ez(barNum))?.let {
    val xPos = pos.pos + systemXGeography.startMain - FINAL_BAR_LINE_WIDTH
    val barLine = barLineArea(BarLineType.START, height)
    area.addArea(barLine, Coord(xPos, start), eas(barNum, part, 0))
  } ?: area
}

private fun DrawableFactory.paintEndBarLine(
  barNum: Int, pos: BarPosition, start: Int, height: Int, part:Int, scoreQuery: ScoreQuery,
  systemXGeography: SystemXGeography, area: Area
): Area {
  val barLine = getBarLine(barNum, height, pos.geog.numBars, scoreQuery)
  val xPos = pos.pos + pos.geog.width + systemXGeography.startMain -
      pos.geog.barEndGeography.keyWidth - pos.geog.barEndGeography.timeWidth
  return area.addArea(barLine, Coord(xPos, start), eas(barNum + pos.geog.numBars-1, part, 0))
}

private fun DrawableFactory.getBarLine(barNum: Int,
                                       height: Int,
                                       numBars: Int,
                                       scoreQuery: ScoreQuery): Area {
  val event = if (barNum + numBars - 1 == scoreQuery.numBars) {
    BarLineType.FINAL
  } else {
    scoreQuery.getEvent(EventType.REPEAT_END, ez(barNum + numBars - 1))?.let {
      BarLineType.FINAL
    } ?: scoreQuery.getEvent(EventType.BARLINE, ez(barNum + numBars - 1))?.subType as (BarLineType?)
    ?: BarLineType.NORMAL
  }
  return barLineArea(event, height).copy(tag = "BarLine")
}