package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.defaultInstrument
import com.philblandford.kscore.api.instrument
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.emptyEventMap
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.util.add
import com.philblandford.kscore.engine.util.removeAt


data class Part(
  val staves: List<Stave> = listOf(Stave()),
  override val eventMap: EventMap = emptyEventMap()
) : ScoreLevelImpl() {

  override val subLevels = staves

  val mainInstrument = eventMap.getEvent(EventType.INSTRUMENT, ez(1))?.let { instrument(it.params) }
    ?: defaultInstrument()
  val label =
    eventMap.getParam<String>(EventType.PART, EventParam.LABEL) ?: mainInstrument.label
  val abbreviation = eventMap.getParam<String>(EventType.PART, EventParam.ABBREVIATION)
    ?: mainInstrument.abbreviation

  override fun getSubLevel(eventAddress: EventAddress): ScoreLevel? {
    return staves.getOrNull(eventAddress.staveId.sub - 1)
  }

  override fun getAllSubLevels(): Iterable<ScoreLevel> {
    return staves
  }

  override fun subLevelIdx(eventAddress: EventAddress): Int {
    return eventAddress.staveId.sub
  }

  override fun badgeEventAddress(eventAddress: EventAddress, levelIdx: Int): EventAddress {
    return eventAddress.copy(staveId = eventAddress.staveId.copy(sub = levelIdx))
  }

  override fun stripAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    var addr = eventAddress.copy(staveId = StaveId(0, eventAddress.staveId.sub))
    if (eventType == EventType.STAVE_JOIN || eventType == EventType.PLAYBACK_STATE) {
      addr = addr.copy(barNum = 0)
    }
    return addr
  }

  override fun prepareAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    return when (eventType) {
      EventType.INSTRUMENT -> eventAddress.copy(
        staveId = StaveId(
          eventAddress.staveId.main,
          0
        ), voice = 0, id = 0
      )
      EventType.PART -> eZero()
      else -> eventAddress
    }
  }

  override fun getSpecialEvent(eventType: EventType, eventAddress: EventAddress): Event? {
    return when (eventType) {
      EventType.INSTRUMENT -> getInstrument(eventAddress)
      EventType.PART -> getPartEvent().second
      else -> super.getSpecialEvent(eventType, eventAddress)
    }
  }

  override fun getSpecialEventAt(
    eventType: EventType,
    eventAddress: EventAddress
  ): Pair<EventMapKey, Event>? {
    return when (eventType) {
      EventType.INSTRUMENT -> getInstrumentAt(eventAddress)
      EventType.PART -> getPartEvent()
      else -> super.getSpecialEventAt(eventType, eventAddress)
    }
  }

  override fun continueGathering(eventType: EventType): Boolean {
    return when (eventType) {
      EventType.INSTRUMENT -> true
      else -> false
    }
  }

  private fun getPartEvent(): Pair<EventMapKey, Event> {
    val event = eventMap.getEvent(EventType.PART) ?: Event(
      EventType.PART,
      paramMapOf(EventParam.LABEL to label, EventParam.ABBREVIATION to abbreviation)
    )
    return EMK(EventType.PART, eZero()) to event
  }

  private fun getInstrument(eventAddress: EventAddress): Event? {
    return getStave(eventAddress.staveId.sub)?.getEvent(EventType.INSTRUMENT, eventAddress)
      ?: eventMap.getEvent(EventType.INSTRUMENT, eventAddress)
  }

  private fun getInstrumentAt(eventAddress: EventAddress): Pair<EventMapKey, Event>? {
    return getStave(eventAddress.staveId.sub)?.getEventAt(EventType.INSTRUMENT, eventAddress)
      ?: eventMap.getEventAt(EventType.INSTRUMENT, eventAddress)
  }

  override fun replaceSubLevel(scoreLevel: ScoreLevel, index: Int): ScoreLevel {
    return Part(staves.removeAt(index - 1).add(index - 1, scoreLevel as Stave), eventMap)
  }

  override fun replaceSelf(eventMap: EventMap, newSubLevels: List<ScoreLevel>?): ScoreLevel {
    return Part(newSubLevels?.map { it as Stave }?.toList() ?: staves, eventMap)
  }


  fun getNumBars(): Int {
    return staves.firstOrNull()?.bars?.size ?: 0
  }

  fun getStave(num: Int): Stave? {
    return staves.getOrNull(num - 1)
  }

  override val scoreLevelType = ScoreLevelType.PART
  override val subLevelType = ScoreLevelType.STAVE

}

fun part(
  instrument: Instrument,
  numBars: Int,
  timeSignature: TimeSignature = TimeSignature(4, 4)
): Part {
  val staves = instrument.clefs.map { stave(it, numBars, timeSignature) }.toList()
  var em = emptyEventMap().putEvent(ez(1), instrument.toEvent())
  em = em.putEvent(
    eZero(), Event(
      EventType.PART, paramMapOf(
        EventParam.LABEL to instrument.label,
        EventParam.ABBREVIATION to instrument.abbreviation
      )
    )
  )
  return part(staves, em)
}

fun part(
  staves: List<Stave> = listOf(Stave()),
  eventMap: EventMap = emptyEventMap()
): Part {
  var map = eventMap
  eventMap.getParam<Iterable<ClefType>>(EventType.INSTRUMENT, EventParam.CLEF, ez(1))?.let {
    if (it.toList().size > 1) {
      map = map.putEvent(
        eZero(), Event(
          EventType.STAVE_JOIN, paramMapOf(
            EventParam.TYPE to StaveJoinType.GRAND,
            EventParam.NUMBER to 1
          )
        )
      )
    }
  }
  return Part(staves, map)
}