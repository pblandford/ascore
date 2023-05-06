package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.api.Rectangle
import com.philblandford.kscore.api.ScoreArea
import com.philblandford.kscore.engine.core.*
import com.philblandford.kscore.engine.core.area.*
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.select.AreaToShow

data class AreaReturn(val page: Int, val areaMapKey: AreaMapKey, val area: Area) {
  val x = areaMapKey.coord.x
  val y = areaMapKey.coord.y
  val width = area.width
  val height = area.height
}

internal fun Representation.getArea(
  eventType: EventType,
  eventAddress: EventAddress,
  extra: Any? = null,

): AreaReturn? {
  pages.forEach { page ->
    page.base.getArea(eventAddress) { area ->
      area.event?.eventType == eventType &&
          area.addressRequirement == AddressRequirement.EVENT &&
          (extra?.let { area.extra == it } ?: true)
    }
      ?.let { return AreaReturn(page.geography.pageNum, it.first, it.second) }
  }
  return null
}

internal fun Representation.getArea(
  addressRequirement: AddressRequirement,
  eventAddress: EventAddress
): AreaReturn? {
  pages.forEach { page ->
    page.base.getArea(eventAddress) { it.addressRequirement == addressRequirement }
      ?.let { return AreaReturn(page.geography.pageNum, it.first, it.second) }
  }
  return null
}

internal fun Representation.getArea(
  tag: String,
  eventAddress: EventAddress
): Pair<AreaMapKey, Area>? {
  pages.forEach { page ->
    page.base.getArea(eventAddress) { it.tag == tag }?.let { return it }
  }
  return null
}

internal fun Representation.getAreaFromCoord(
  page: Int,
  x: Int,
  y: Int,
  fuzz: Int = 0,
  matchFunc: (Area) -> Boolean = { false }
): Pair<AreaMapKey, Area>? {
  return pages.toList().getOrNull(page - 1)?.base?.findFromCoord(x, y, matchFunc, fuzz)
}

internal fun Representation.getEvent(
  page: Int,
  x: Int,
  y: Int,
  fuzz: Int = 0
): Pair<EventAddress, Event>? {
  return getAreaFromCoord(page, x, y, fuzz) {
    it.addressRequirement == AddressRequirement.EVENT
  }?.let { (k, v) ->
    v.event?.let {
      k.eventAddress to it
    }
  }
}

internal fun Representation.getAreaToShow(
  page: Int,
  x: Int,
  y: Int,
  extra: Any? = null,
  fuzz: Int = 0,
  cond: (Area) -> Boolean = { true }
): AreaToShow? {
  return getAreaFromCoord(page, x, y, fuzz) { area ->
    cond(area) && area.addressRequirement == AddressRequirement.EVENT
        && (extra?.let { area.extra == it } ?: true)
  }?.let { (k, v) ->
    v.event?.let { event ->
      AreaToShow(
        ScoreArea(page, Rectangle(k.coord.x - v.xMargin, k.coord.y - v.yMargin, v.width, v.height)),
        k.eventAddress,
        event,
        v.extra
      )
    }
  }
}

internal fun Representation.getEvent(
  page: Int, x: Int, y: Int, eventType: EventType,
  addressRequirement: AddressRequirement = AddressRequirement.EVENT
): Pair<EventAddress, Event>? {
  return getAreaFromCoord(page, x, y) {
    it.addressRequirement == addressRequirement &&
        it.event?.eventType == eventType
  }?.let { (k, v) ->
    v.event?.let {
      k.eventAddress to it
    }
  }
}

internal fun Representation.getArea(
  page: Int,
  x: Int,
  y: Int,
  addressRequirement: AddressRequirement,
  fuzz: Int = 0
): Pair<AreaMapKey, Area>? {
  return getAreaFromCoord(page, x, y, fuzz) { it.addressRequirement == addressRequirement }
}

internal fun Representation.isAboveStave(page: Int, x: Int, y: Int): Boolean? {
  return getAreaFromCoord(
    page,
    x,
    y
  ) { it.addressRequirement == AddressRequirement.SEGMENT }?.let { (k, a) ->
    val midPoint = k.coord.y + a.yMargin + BLOCK_HEIGHT * 5
    y < midPoint
  } ?: false
}

internal fun Representation.getAreasAtAddress(eventAddress: EventAddress): List<AreaToShow> {

  if (eventAddress == eZero()) {
    return getMetaAreas()
  }
  val address = eventAddress.voiceless()
  return getPage(address)?.let { page ->
    val map =
      page.base.getAreasAtAddress(address, ez(page.geography.startBar), Coord(), areaMapOf())

    map.mapNotNull { (k, v) ->
      val childStart = v.childMap.minByOrNull { it.first.coord.y }?.first?.coord?.y ?: 0
      val childEnd = v.childMap.maxByOrNull { it.first.coord.y + it.second.height }?.let {
        it.first.coord.y + it.second.height
      } ?: v.height

      v.event?.let { ev ->
        AreaToShow(
          ScoreArea(
            page.geography.pageNum, Rectangle(
              k.coord.x - v.xMargin, k.coord.y + childStart,
              v.width, childEnd - childStart
            )
          ), k.eventAddress.copy(staveId = address.staveId), ev
        )
      }
    }
  } ?: listOf()
}

private fun Representation.getMetaAreas(): List<AreaToShow> {

  return getArea("TitleArea", eZero())?.let { titleArea ->
    titleArea.second.childMap.mapNotNull { (k, v) ->
      v.event?.let { ev ->
        AreaToShow(
          ScoreArea(
            1, Rectangle(
              k.coord.x + titleArea.first.coord.x,
              k.coord.y + titleArea.first.coord.y, v.width, v.height
            )
          ), k.eventAddress, ev, v.extra
        )
      }
    }
  } ?: listOf()
}

private fun Area.getAreasAtAddress(
  eventAddress: EventAddress, areaAddress: EventAddress,
  offset: Coord, mapSoFar: AreaMap
): AreaMap {

  var newMap = mapSoFar
  if (inRange(eventAddress, areaAddress, this)) {
    childMap.forEach { (k, v) ->
      newMap = newMap.plus(getEvents(k, v, eventAddress, offset)).distinctBy { it.first }
      newMap =
        newMap.plus(v.getAreasAtAddress(eventAddress, k.eventAddress, k.coord.plus(offset), newMap)).distinctBy { it.first }
    }
  }
  return newMap
}

private fun inRange(eventAddress: EventAddress, areaAddress: EventAddress, area: Area): Boolean {
  val lastMatch = (area.tag == "Bar" || area.tag == "BarEnd") &&
      (eventAddress.barNum >= areaAddress.barNum + 1 && eventAddress.barNum <= areaAddress.barNum + 1 + area.numBars - 1)
  return lastMatch || (eventAddress.barNum >= areaAddress.barNum && eventAddress.barNum <= areaAddress.barNum + area.numBars - 1)
}

private fun Representation.getPage(eventAddress: EventAddress): PageArea? {
  return pages.find { it.geography.startBar <= eventAddress.barNum && it.geography.endBar >= eventAddress.barNum }
}

private fun getEvents(
  areaMapKey: AreaMapKey, area: Area, eventAddress: EventAddress,
  offset: Coord = Coord()
): AreaMap {
  return area.event?.let { event ->
    if (matchAddress(areaMapKey.eventAddress.idless(), eventAddress.idless(), event.eventType)) {
      areaMapOf(areaMapKey.copy(coord = areaMapKey.coord.plus(offset)) to area)
    } else null
  } ?: areaMapOf()
}

private fun matchAddress(a1: EventAddress, a2: EventAddress, eventType: EventType): Boolean {

  return when (eventType) {
    EventType.DURATION, EventType.ORNAMENT, EventType.ARTICULATION, EventType.TUPLET,
    EventType.FINGERING, EventType.BOWING -> a1.voiceless() == a2.voiceless()
    EventType.TIE, EventType.NOTE, EventType.LYRIC -> a1.voiceIdless() == a2.voiceIdless()
    EventType.BAR -> false
    EventType.TEMPO, EventType.TEMPO_TEXT -> a1.staveless() == a2.staveless()
    EventType.VOLTA -> a1.start().staveless() == a2.start().staveless()
    EventType.BARLINE, EventType.REPEAT_START, EventType.REPEAT_END -> false
    else -> a1 == a2
  }
}

internal fun Representation.getEventAddress(
  page: Int,
  x: Int,
  y: Int,
  addressRequirement: AddressRequirement,
  fuzz: Int = 0
): EventAddress? {
  return when (addressRequirement) {
    AddressRequirement.SEGMENT -> getEventAddressSegment(page, x, y)
    AddressRequirement.EVENT -> getEventAddressEvent(page, x, y, fuzz)
    else -> null
  }
}

private fun Representation.getEventAddressEvent(
  page: Int,
  x: Int,
  y: Int,
  fuzz: Int = 0
): EventAddress? {
  return getArea(page, x, y, AddressRequirement.EVENT, fuzz)?.first?.eventAddress
}


private fun Representation.getEventAddressSegment(page: Int, x: Int, y: Int): EventAddress? {
  return pages.getOrNull(page - 1)?.let { pageArea ->
    pageArea.geography.getSystem(x, y)?.let { sqr ->
      sqr.geography.getPart(sqr.x, sqr.y)?.let { pqr ->
        pqr.geography.getStave(pqr.x, pqr.y, pqr.eventAddress)?.let { stqr ->
          stqr.geography.getBar(stqr.x, stqr.y, stqr.eventAddress)?.let { bqr ->
            bqr.geography.getSlice(bqr.x, bqr.y, bqr.eventAddress)
          }
        }
      }
    }
  }
}

private fun PageGeography.getSystem(x: Int, y: Int): QueryReturn<SystemYGeography>? {
  return systemPositions.toList().find { (_, sysPos) ->
    y >= sysPos.pos - sysPos.systemYGeography.yMargin
        && y <= sysPos.pos + sysPos.systemYGeography.height
  }?.let {
    QueryReturn(
      it.second.systemYGeography, x - layoutDescriptor.leftMargin,
      y - it.second.pos,
      eZero()
    )
  }
}

private fun SystemYGeography.getPart(x: Int, y: Int): QueryReturn<PartGeography>? {
  return partPositions.toList().find { (_, partPos) ->
    y >= partPos.pos - partPos.partGeography.yMargin
        && y <= partPos.pos + partPos.partGeography.height - partPos.partGeography.yMargin
  }?.let {
    QueryReturn(
      it.second.partGeography, x - this.xGeog.preHeaderLen, y - it.second.pos,
      eZero().copy(staveId = StaveId(it.first, 0))
    )
  }
}

private fun PartGeography.getStave(
  x: Int,
  y: Int,
  eventAddress: EventAddress
): QueryReturn<StaveGeography>? {
  return stavePositions.toList().find { (_, stavePos) ->
    y >= stavePos.pos - stavePos.staveGeography.yMargin
        && y <= stavePos.pos + stavePos.staveGeography.height - stavePos.staveGeography.yMargin
  }?.let {
    QueryReturn(
      it.second.staveGeography, x, y - it.second.pos,
      eventAddress.copy(staveId = eventAddress.staveId.copy(sub = it.first))
    )
  }
}

private fun StaveGeography.getBar(
  x: Int,
  y: Int,
  eventAddress: EventAddress
): QueryReturn<ResolvedBarGeography>? {
  return barPositions.toList().find { (_, barPos) ->
    x >= barPos.pos + this.headerLen && x <= barPos.pos + barPos.geog.width + this.headerLen
  }?.let {
    QueryReturn(
      it.second.geog, x - it.second.pos - this.headerLen, y,
      eventAddress.copy(barNum = it.first)
    )
  }
}

private fun ResolvedBarGeography.getSlice(x: Int, y: Int, eventAddress: EventAddress): EventAddress? {
  val sliceList = original.slicePositions.toList().sortedBy { it.first }
  val resolvedX = x - this.barStartGeography.width
  val hz = sliceList.windowed(2).find() { (l1, l2) ->

    resolvedX >= l1.second.start && x <= l2.second.start
  }?.first()?.first ?: sliceList.lastOrNull()?.let { if (resolvedX > it.second.xMargin) it.first else null }
  ?: Horizontal(0, dZero())
  return eventAddress.copy(offset = hz.offset, graceOffset = hz.graceOffset)
}

private data class QueryReturn<T : Geography>(
  val geography: T, val x: Int, val y: Int,
  val eventAddress: EventAddress
)