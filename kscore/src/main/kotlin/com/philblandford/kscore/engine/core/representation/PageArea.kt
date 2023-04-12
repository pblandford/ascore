package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.PageGeography
import com.philblandford.kscore.engine.core.SystemPosition
import com.philblandford.kscore.engine.core.SystemYGeography
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLogt

data class PageArea(val base: Area, val geography: PageGeography)

internal fun DrawableFactory.createPages(
  partQuery: PartQuery, scoreQuery: ScoreQuery, geographyYQuery: GeographyYQuery,
  layoutDescriptor: LayoutDescriptor
): List<PageArea> {
  val pages = mutableListOf<PageArea>()

  val breaks = scoreQuery.getEvents(EventType.BREAK)?.filter { it.value.subType == BreakType.PAGE }
    ?.map { it.key.eventAddress.barNum }?.toSet() ?: setOf()

  var remaining = geographyYQuery.getSystemYGeographies().toMutableList()
  var layout = layoutDescriptor
  var num = 1

  while (remaining.isNotEmpty()) {
    val res =
      pageArea(
        partQuery, scoreQuery, remaining, layout, num,
        breaks
      )
    val pageArea = res.first
    layout = res.second
    if (pageArea != null) {
      pages.add(pageArea)
      num++
      remaining =
        remaining.dropWhile { it.xGeog.startBar <= pageArea.geography.systemPositions.lastKey() }
          .toMutableList()
    } else {
      break
    }
  }

  return pages
}

private typealias SystemPositionMap = Map<Int, SystemPosition>

internal fun DrawableFactory.pageArea(
  partQuery: PartQuery, scoreQuery: ScoreQuery, geographies: List<SystemYGeography>,
  layoutDescriptor: LayoutDescriptor, num: Int, breaks: Set<Int>
): Pair<PageArea?, LayoutDescriptor> {
  var area =
    Area(tag = "Page", height = layoutDescriptor.topMargin)

  if (num == 1) {
    area = addTitleArea(area, scoreQuery, layoutDescriptor)
  }
  val pair = addSystems(area, geographies, partQuery, scoreQuery, layoutDescriptor, breaks)
  val systemPositions = pair.first
  area = pair.second

  area = addFooter(area, layoutDescriptor, num)

  val geography = PageGeography(num, layoutDescriptor, systemPositions.toSortedMap())
  area = area.copy(numBars = geographies.last().xGeog.endBar - geographies.first().xGeog.startBar)

  val layout = if (area.height > layoutDescriptor.pageHeight) {
    layoutDescriptor.copy(pageHeight = area.height)
  } else layoutDescriptor

  area = area.copy(height = layout.pageHeight)
  val page = if (systemPositions.isNotEmpty()) PageArea(area, geography) else null
  return Pair(page, layout)
}

private fun DrawableFactory.addTitleArea(
  area: Area, scoreQuery: ScoreQuery,
  layoutDescriptor: LayoutDescriptor
): Area {
  val title =
    titleArea(scoreQuery, layoutDescriptor, scoreQuery.selectedPartName())
  return area.addBelow(title, x = layoutDescriptor.leftMargin)
}

private fun DrawableFactory.addFooter(
  pageArea: Area,
  layoutDescriptor: LayoutDescriptor,
  num: Int
): Area {
  val footer = pageFooterArea(num, layoutDescriptor.pageWidth, layoutDescriptor.bottomMargin)
  val y = if (pageArea.height > layoutDescriptor.pageHeight - layoutDescriptor.bottomMargin) {
    pageArea.height
  } else {
    layoutDescriptor.pageHeight - layoutDescriptor.bottomMargin
  }
  return pageArea.addArea(footer, Coord(0, y), ez(num))
}

private fun DrawableFactory.addSystems(
  pageArea: Area, geographies: List<SystemYGeography>,
  partQuery: PartQuery, eventGetter: EventGetter, layoutDescriptor: LayoutDescriptor,
  breaks: Set<Int>
): Pair<SystemPositionMap, Area> {

  val staveJoins = getStaveJoins(partQuery, eventGetter)
  val available =
    layoutDescriptor.pageHeight - layoutDescriptor.bottomMargin - layoutDescriptor.topMargin
  val systemPositions = getSystemPositions(available, pageArea.height, geographies, breaks)

  val area = systemPositions.toList().fold(pageArea) { pa, (_,sp) ->
    val sysArea = systemArea(partQuery, sp.systemYGeography, staveJoins)
    pa.addArea(sysArea, Coord(layoutDescriptor.leftMargin, sp.pos),
      ez(sp.systemYGeography.xGeog.startBar))
  }

  return systemPositions.toMap() to area
}

private fun getSystemPositions(
  available: Int, offset:Int, geographies: List<SystemYGeography>,
  breaks: Set<Int>
): List<Pair<Int, SystemPosition>> {
  val systemPositions = mutableListOf<Pair<Int, SystemPosition>>()
  var totalHeight = offset

  run loop@{
    geographies.forEach { sysYGeog ->

      if (systemPositions.isNotEmpty() && breaks.contains(sysYGeog.xGeog.startBar - 1)) {
        return@loop
      }

      if (totalHeight + sysYGeog.height + SYSTEM_GAP > available && systemPositions.isNotEmpty()) {
        return@loop
      }

      systemPositions.add(sysYGeog.xGeog.startBar to  SystemPosition(totalHeight + SYSTEM_GAP, sysYGeog))
      totalHeight += sysYGeog.height + SYSTEM_GAP
    }
  }
  val systemList = systemPositions.toList().sortedBy { it.first }
  val endSystems = systemList.lastOrNull()?.let {
    it.second.pos + it.second.systemYGeography.height - it.second.systemYGeography.yMargin
  } ?: 0
  val leftOver = available - endSystems
  val extraY = leftOver / systemPositions.size
  ksLogt("$extraY $available")
  val spacedSystems = if (extraY < available / 15) {
    (listOf(systemList.first()) + systemList.drop(1)).withIndex().map { iv ->
      iv.value.first to
          iv.value.second.copy(pos = iv.value.second.pos + extraY * iv.index)
    }
  } else {
    systemList
  }
  return spacedSystems
}

internal fun getSystemPosition(
  eventAddress: EventAddress,
  pageGeography: PageGeography
): Pair<Int, SystemPosition>? {
  return pageGeography.systemPositions.toList().find {
    eventAddress.barNum in (it.first..it.second.systemYGeography.xGeog.endBar)
  }
}

internal fun PageGeography.getSlicePosition(eventAddress: EventAddress): Coord? {
  return getSystemPosition(eventAddress, this)?.let { sysPos ->
    sysPos.second.systemYGeography.xGeog.barPositions[eventAddress.barNum]?.let { barPos ->
      val startBar =
        layoutDescriptor.leftMargin + sysPos.second.systemYGeography.xGeog.startMain + barPos.pos
      val y = sysPos.second.pos
      barPos.geog.original.slicePositions[hz(eventAddress.offset)]?.let { slicePos ->
        val x = startBar + slicePos.start
        Coord(x, y)
      } ?: run {
        if (barPos.geog.original.slicePositions.isEmpty()) {
          Coord(startBar, y)
        } else null
      }
    }
  }
}

internal fun getBottomSystem(eventAddress: EventAddress, pageGeography: PageGeography): Int? {
  return getSystemPosition(eventAddress, pageGeography)?.let { sysPos ->
    sysPos.second.systemYGeography.partPositions.toList().maxByOrNull { it.first }
      ?.let { lastPart ->
        lastPart.second.partGeography.stavePositions.toList()
          .maxByOrNull { it.first }?.let { lastStave ->
            sysPos.second.pos +
                lastPart.second.pos + lastPart.second.partGeography.yMargin +
                lastStave.second.pos + lastStave.second.staveGeography.yMargin + STAVE_HEIGHT
          }
      }
  }
}

private fun getStaveJoins(partQuery: PartQuery, eventGetter: EventGetter): EventHash {
  return partQuery.getParts().map { it.key.staveId.main }.distinct().toList()
    .fold(eventHashOf()) { map, partNum ->
      val addr = ez(0).copy(staveId = StaveId(partNum, 0))
      eventGetter.getEvent(EventType.STAVE_JOIN, addr)?.let {
        map.plus(EventMapKey(EventType.STAVE_JOIN, addr) to it)
      } ?: map

    }
}