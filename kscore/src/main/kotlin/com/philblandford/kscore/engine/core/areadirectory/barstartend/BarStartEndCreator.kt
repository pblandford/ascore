package com.philblandford.kscore.engine.core.areadirectory.barstartend

import com.philblandford.kscore.engine.core.BarEndGeography
import com.philblandford.kscore.engine.core.BarEndGeographyPair
import com.philblandford.kscore.engine.core.BarStartGeography
import com.philblandford.kscore.engine.core.BarStartGeographyPair
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.AreaDirectory
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.REPEAT_DOT_WIDTH
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.EventHashSimple
import com.philblandford.kscore.engine.types.*

data class BarStartArea(val base: Area)
data class BarEndArea(val base: Area)

fun DrawableFactory.createBarStarts(scoreQuery: ScoreQuery, existing: AreaDirectory?,
                    updateHeaders: Boolean): Pair<Lookup<BarStartAreaPair>, Lookup<BarStartGeographyPair>> {

  existing?.let {
    if (!updateHeaders) {
      return Pair(it.barStartLookup, it.barStartGeogLookup)
    }
  }


  val barStartAreas = createBarStartAreas(scoreQuery)
  val barStartGeographies = createBarStartGeographies(scoreQuery, barStartAreas)
  val alignedBarStartAreas = alignBarStartAreas(barStartAreas, barStartGeographies)

  return Pair(alignedBarStartAreas, barStartGeographies)
}

private fun DrawableFactory.createBarStartAreas(scoreQuery: ScoreQuery): Lookup<BarStartAreaPair> {
  return (1..scoreQuery.numBars).flatMap { bar ->
    scoreQuery.getAllStaves(true).map { stave ->
      val addr = ea(bar).copy(staveId = stave)
      val hash = getEventsForBar(addr, true, scoreQuery)
      eas(bar, dZero(), stave) to barStartAreaPair(hash)
    }
  }.toMap()
}

private fun createBarStartGeographies(
  scoreQuery: ScoreQuery,
  areas: Lookup<BarStartAreaPair>
): Lookup<BarStartGeographyPair> {
  val grouped = areas.toList().groupBy { it.first.barNum }
  return (1..scoreQuery.numBars).map { bar ->
    ez(bar) to createBarStartGeographyPair(
      grouped[bar]?.map { it.second }?.toList() ?: listOf(),
      getEventsForBar(ea(bar), true, scoreQuery)
    )
  }.toMap()
}

private fun alignBarStartAreas(
  areas: Lookup<BarStartAreaPair>,
  geographies: Lookup<BarStartGeographyPair>
): Lookup<BarStartAreaPair> {
  val groupedAreas = areas.toList().groupBy { it.first.barNum }

  return groupedAreas.flatMap { (bar, lookup) ->
    geographies[ez(bar)]?.let { geog ->
      alignForBar(lookup.toMap(), geog).toList()
    } ?: listOf()
  }.toMap()
}

private fun alignForBar(
  areas: Lookup<BarStartAreaPair>,
  geographyPair: BarStartGeographyPair
): Lookup<BarStartAreaPair> {
  return areas.map { (ea, pair) ->
    ea to alignPair(pair, geographyPair)
  }.toMap()
}

private fun alignPair(
  barStartAreaPair: BarStartAreaPair,
  barStartGeographyPair: BarStartGeographyPair
): BarStartAreaPair {
  val startStave = alignBarStart(barStartAreaPair.startStave, barStartGeographyPair.startStave)
  val notStartStave =
    alignBarStart(barStartAreaPair.notStartStave, barStartGeographyPair.notStartStave)
  return BarStartAreaPair(startStave, notStartStave)
}

private fun alignBarStart(
  barStartArea: BarStartArea,
  barStartGeography: BarStartGeography
): BarStartArea {
  var base = Area(tag = barStartArea.base.tag)
  barStartArea.base.findByTagSingle("StartRepeat")?.let {
    base = base.addArea(it, Coord(barStartGeography.barLineWidth, (BLOCK_HEIGHT * 2.5).toInt()))
  }
  barStartArea.base.findByTagSingle("KeySignature")?.let {
    base = base.addArea(it, Coord(barStartGeography.keyStart))
  }
  barStartArea.base.findByTagSingle("TimeSignature")?.let {
    base = base.addArea(it, Coord(barStartGeography.timeStart))
  }
  return BarStartArea(base)
}


fun DrawableFactory.createBarEnds(scoreQuery: ScoreQuery, existing:AreaDirectory?,
                  updateHeaders:Boolean): Pair<Lookup<BarEndAreaPair>, Lookup<BarEndGeographyPair>> {

  existing?.let {
    if (!updateHeaders) {
      return Pair(it.barEndLookup, it.barEndGeogLookup)
    }
  }

  val barEndAreas = mutableMapOf<EventAddress, BarEndAreaPair>()
  val barEndGeographies = mutableMapOf<EventAddress, BarEndGeographyPair>()

  (1..scoreQuery.numBars).map { bar ->
    val areas = mutableMapOf<EventAddress, BarEndAreaPair>()
    scoreQuery.getAllStaves(true).forEach { stave ->
      val addr = ea(bar).copy(staveId = stave)
      val hash = getEventsForBar(addr, false, scoreQuery)
      areas.put(addr, barEndArea(hash))
    }
    barEndAreas.putAll(areas)
    barEndGeographies.put(
      ez(bar), createBarEndGeographyPair(
        areas.values.toList(),
        getEventsForBar(ez(bar), false, scoreQuery)
      )
    )
  }
  return Pair(barEndAreas, barEndGeographies)
}

private fun createBarStartGeographyPair(
  pairs: Iterable<BarStartAreaPair>,
  events: EventHashSimple
): BarStartGeographyPair {
  val startStave = createBarStartGeography(pairs.map { it.startStave }, events)
  val notStartStave = createBarStartGeography(pairs.map { it.notStartStave }, events)
  return BarStartGeographyPair(startStave, notStartStave)
}

private fun createBarStartGeography(
  areas: Iterable<BarStartArea>,
  events: EventHashSimple
): BarStartGeography {
  val isRepeat = events.containsKey(EventType.REPEAT_START)
  val barLineDotWidth = if (isRepeat) {
    Pair(barLineWidth(BarLineType.START), REPEAT_DOT_WIDTH * 2)
  } else {
    Pair(0, 0)
  }
  val keyAreas = areas.flatMap { it.base.findByTag("KeySignature").map { it.value } }
  val timeAreas = areas.flatMap { it.base.findByTag("TimeSignature").map { it.value } }
  val keyWidth = keyAreas.toList().maxByOrNull { it.width }?.width ?: 0
  val timeWidth = timeAreas.toList().maxByOrNull { it.width }?.width ?: 0

  return BarStartGeography(barLineDotWidth.first, barLineDotWidth.second, keyWidth, timeWidth)
}


private fun createBarEndGeographyPair(
  pairs: Iterable<BarEndAreaPair>,
  events: EventHashSimple
): BarEndGeographyPair {
  val endStave = createBarEndGeography(pairs.map { it.endStave }, events)
  val notEndStave = createBarEndGeography(pairs.map { it.notEndStave }, events)
  return BarEndGeographyPair(endStave, notEndStave)
}


private fun createBarEndGeography(
  areas: Iterable<BarEndArea>,
  events: EventHashSimple
): BarEndGeography {

  val isRepeat = events.containsKey(EventType.REPEAT_END)
  val barLineWidth = getBarLineWidth(events, isRepeat)
  val keyAreas = areas.flatMap { it.base.findByTag("KeySignature").map { it.value } }
  val timeAreas = areas.flatMap { it.base.findByTag("TimeSignature").map { it.value } }
  val clefAreas = areas.flatMap { it.base.findByTag("Clef").map { it.value } }
  val keyWidth = keyAreas.toList().maxByOrNull { it.width }?.width ?: 0
  val timeWidth = timeAreas.toList().maxByOrNull { it.width }?.width ?: 0
  val clefWidth = clefAreas.toList().maxByOrNull { it.width }?.width ?: 0
  return BarEndGeography(
    keyWidth,
    timeWidth,
    clefWidth,
    if (isRepeat) REPEAT_DOT_WIDTH * 2 else 0,
    barLineWidth
  )
}


private fun alignBarEndAreas(
  areas: Lookup<BarEndAreaPair>,
  geographies: Lookup<BarEndGeographyPair>
): Lookup<BarEndAreaPair> {
  val groupedAreas = areas.toList().groupBy { it.first.barNum }

  return groupedAreas.flatMap { (bar, lookup) ->
    geographies[ez(bar)]?.let { geog ->
      alignForBar(lookup.toMap(), geog).toList()
    } ?: listOf()
  }.toMap()
}

private fun alignForBar(
  areas: Lookup<BarEndAreaPair>,
  geographyPair: BarEndGeographyPair
): Lookup<BarEndAreaPair> {
  return areas.map { (ea, pair) ->
    ea to alignPair(pair, geographyPair)
  }.toMap()
}

private fun alignPair(
  barEndAreaPair: BarEndAreaPair,
  barEndGeographyPair: BarEndGeographyPair
): BarEndAreaPair {
  val endStave = alignBarEnd(barEndAreaPair.endStave, barEndGeographyPair.endStave)
  val notEndStave =
    alignBarEnd(barEndAreaPair.notEndStave, barEndGeographyPair.notEndStave)
  return BarEndAreaPair(endStave, notEndStave)
}

private fun alignBarEnd(
  barEndArea: BarEndArea,
  barEndGeography: BarEndGeography
): BarEndArea {
  var base = Area()

  barEndArea.base.findByTagSingle("KeySignature")?.let {
    base = base.addArea(it, Coord(barEndGeography.keyStart))
  }
  barEndArea.base.findByTagSingle("TimeSignature")?.let {
    base = base.addArea(it, Coord(barEndGeography.timeStart))
  }
  barEndArea.base.findByTagSingle("Clef")?.let {
    base = base.addArea(it, Coord(barEndGeography.clefStart))
  }
  barEndArea.base.findByTagSingle("EndRepeat")?.let {
    base = base.addArea(it, Coord(barEndGeography.repeatDotStart))
  }
  return BarEndArea(base)
}


private fun getBarLineWidth(events: EventHashSimple, isRepeat: Boolean): Int {
  return if (isRepeat) {
    barLineWidth(BarLineType.FINAL)
  } else {
    (events[EventType.BARLINE]?.subType as BarLineType?)?.let {
      barLineWidth(it)
    } ?: 0
  }
}

private fun getEventsForBar(
  eventAddress: EventAddress,
  startBar: Boolean,
  scoreQuery: ScoreQuery
): EventHashSimple {
  val addr = if (!startBar) eventAddress.inc() else eventAddress
  var events = setOf(
    EventType.TIME_SIGNATURE,
    EventType.REPEAT_START, EventType.CLEF
  ).mapNotNull { type ->
    scoreQuery.getEvent(type, addr)?.let { type to it }
  }.filterNot { it.second.isTrue(EventParam.HIDDEN) }.toMap()
  getKeyWithPrevious(addr, scoreQuery)?.let {
    events = events.plus(EventType.KEY_SIGNATURE to it)
  }
  if (!startBar) {
    scoreQuery.getEvent(EventType.REPEAT_END, eventAddress)
      ?.let { events = events.plus(it.eventType to it) }
    scoreQuery.getEvent(EventType.BARLINE, eventAddress)?.let {
      events = events.plus(it.eventType to it)
    }
  }
  if (!startBar && eventAddress.barNum == scoreQuery.numBars) {
    events = events.plus(
      EventType.BARLINE to Event(
        EventType.BARLINE,
        paramMapOf(EventParam.TYPE to BarLineType.FINAL)
      )
    )
  }
  return events.toMap()
}

private fun getKeyWithPrevious(eventAddress: EventAddress, scoreQuery: ScoreQuery): Event? {
  return scoreQuery.getEvent(EventType.KEY_SIGNATURE, eventAddress)?.let { ks ->
    val clef = scoreQuery.getParamAt<ClefType>(EventType.CLEF, EventParam.TYPE, eventAddress)
      ?: ClefType.TREBLE
    val previous =
      scoreQuery.getParamAt<Int>(EventType.KEY_SIGNATURE, EventParam.SHARPS, eventAddress.inc(-1))
    ks.addParam(EventParam.PREVIOUS_SHARPS, previous).addParam(EventParam.CLEF, clef)
  }
}