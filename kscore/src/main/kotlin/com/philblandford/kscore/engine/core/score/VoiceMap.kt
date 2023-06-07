package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.beam.BeamMap
import com.philblandford.kscore.engine.beam.beam
import com.philblandford.kscore.engine.beam.beamMapOf
import com.philblandford.kscore.engine.beam.createBeams
import com.philblandford.kscore.engine.dsl.empty
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.dsl.tupletMarker
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.*
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.time.timeSignature
import com.philblandford.kscore.engine.types.*
import java.util.*

data class EventListKey(val offset: Duration, val event: Event)
private typealias EventList = List<EventListKey>

typealias VoiceEventMap = SortedMap<Duration, Event>

fun VoiceEventMap.eventString(): String {
  return toList()
    .joinToString(separator = "")
    { it.second.asString() + ":" }.dropLastWhile { it == ':' }
}

open class VoiceMap internal constructor(

  open val timeSignature: TimeSignature = TimeSignature(4, 4),
  override val eventMap: EventMap =
    emptyEventMap().putEvent(eZero(), timeSignature.toEvent()),
  val tuplets: List<Tuplet> = listOf()
) : ScoreLevelImpl() {

  override val subLevels = tuplets

  fun actualDuration(): Duration {
    return eventMap.getEvents(EventType.DURATION)?.let { durationEvents ->
      durationEvents.toList().maxByOrNull { it.first.eventAddress.offset }?.let { last ->
        last.first.eventAddress.offset.add(last.second.realDuration())
      }
    } ?: dZero()
  }

  override fun replaceSelf(eventMap: EventMap, newSubLevels: List<ScoreLevel>?): ScoreLevel {
    val ts = eventMap.getEvent(EventType.TIME_SIGNATURE)?.let { timeSignature(it) } ?: timeSignature
    return voiceMap(ts, eventMap, newSubLevels?.map { it as Tuplet } ?: tuplets)
  }

  override fun getSubLevel(eventAddress: EventAddress): ScoreLevel? {
    return tuplets.find {
      it.offset <= eventAddress.offset &&
          it.offset.add(it.realDuration) > eventAddress.offset
    }
  }

  override fun getAllSubLevels(): Iterable<ScoreLevel> {
    return tuplets
  }

  override fun subLevelIdx(eventAddress: EventAddress): Int {
    return tuplets.toList().indexOfFirst { it.offset == eventAddress.offset }
  }

  override fun replaceSubLevel(scoreLevel: ScoreLevel, index: Int): ScoreLevel {
    return when (scoreLevel) {
      is Tuplet -> {
        val newTuplets = tuplets.filterNot { it.offset == scoreLevel.offset }.plus(scoreLevel)
          .sortedBy { it.offset }

        VoiceMap(timeSignature, eventMap, newTuplets)
      }
      else -> this
    }
  }

  override fun getAllLevelEvents(): EventHash {
    return eventMap.collateEvents(listOf(EventType.DURATION, EventType.LYRIC, EventType.TIE))
      ?: eventHashOf()
  }

  fun eventString(): String {
    return toVoiceEvents(getEvents(EventType.DURATION) ?: eventHashOf()).eventString()
  }

  override fun toString(): String {
    val durations = eventString()
    return "${timeSignature.numerator}/${timeSignature.denominator} $durations"
  }

  override fun getSpecialEvent(eventType: EventType, eventAddress: EventAddress): Event? {
    return when (eventType) {
      /* Query our tuplets first, then our own map */
      EventType.DURATION, EventType.TIE -> {
        return getSubLevel(eventAddress)?.getEvent(eventType, eventAddress) ?: eventMap.getEvent(
          eventType,
          eventAddress
        )?.let { it.addParam(EventParam.REAL_DURATION, it.duration()) }
      }
      EventType.NOTE -> getNote(eventAddress)
      EventType.TUPLET -> getTuplet(eventAddress)
      EventType.ORNAMENT -> getOrnament(eventAddress)
      EventType.ARTICULATION -> getArticulation(eventAddress)
      EventType.BOWING -> getBowing(eventAddress)
      EventType.FINGERING -> getFingering(eventAddress)
      else -> null
    }
  }

  override fun getSpecialEventAt(
    eventType: EventType,
    eventAddress: EventAddress
  ): Pair<EventMapKey, Event>? {
    return when (eventType) {
      EventType.TUPLET -> getSubLevel(eventAddress)?.let {
        EMK(
          EventType.TUPLET,
          eventAddress.copy(offset = (it as Tuplet).offset)
        ) to it.toEvent()
      }
      else -> null
    }
  }

  private fun getNote(eventAddress: EventAddress): Event? {
    return getSpecialEvent(
      EventType.DURATION,
      eventAddress.copy(id = 0)
    )?.params?.g<Iterable<Event>>(EventParam.NOTES)
      ?.toList()?.getOrNull(eventAddress.id - 1)
  }

  private fun getOrnament(eventAddress: EventAddress): Event? {
    return getParam<ChordDecoration<Ornament>>(
      EventType.DURATION,
      EventParam.ORNAMENT,
      eventAddress
    )?.items?.first()?.toEvent()
  }

  private fun getArticulation(eventAddress: EventAddress): Event? {
    return getParam<ChordDecoration<ArticulationType>>(
      EventType.DURATION,
      EventParam.ARTICULATION,
      eventAddress
    )?.items?.first()?.let {
      Event(EventType.ARTICULATION, paramMapOf(EventParam.TYPE to it))
    }
  }

  private fun getBowing(eventAddress: EventAddress): Event? {
    return getParam<ChordDecoration<BowingType>>(
      EventType.DURATION,
      EventParam.BOWING,

      eventAddress
    )?.items?.first()?.let {
      Event(EventType.BOWING, paramMapOf(EventParam.TYPE to it))
    }
  }

  private fun getFingering(eventAddress: EventAddress): Event? {
    return getParam<ChordDecoration<Int>>(
      EventType.DURATION,
      EventParam.FINGERING,
      eventAddress
    )?.items?.let { nums ->
      Event(EventType.FINGERING, paramMapOf(EventParam.NUMBER to nums))
    }
  }

  private fun getTuplet(eventAddress: EventAddress): Event? {
    return getStartTuplet(eventAddress) ?: getEndTuplet(eventAddress)
  }

  private fun getStartTuplet(eventAddress: EventAddress): Event? {
    return tuplets.find { it.offset == eventAddress.offset }?.toEvent()
  }

  private fun getEndTuplet(eventAddress: EventAddress): Event? {
    return tuplets.find {
      it.lastMember == eventAddress.offset
    }?.let { tuplet ->
      val ts = tuplet.timeSignature
      Event(
        EventType.TUPLET, paramMapOf(
          EventParam.NUMERATOR to ts.numerator,
          EventParam.DENOMINATOR to ts.denominator,
          EventParam.END to true
        )
      )
    }
  }

  override fun getSpecialEvents(eventType: EventType): EventHash? {
    return when (eventType) {
      EventType.DURATION, EventType.TIE -> eventMap.getEvents(eventType)?.plus(
        getTupletEvents(eventType)
      )
      EventType.NOTE -> getSpecialEvents(EventType.DURATION)?.flatMap { (k, e) ->
        chord(e)?.let {
          it.notes.withIndex().map { iv ->
            k.copy(eventAddress = k.eventAddress.copy(id = iv.index + 1), eventType = eventType) to iv.value.toEvent()
          }
        } ?: listOf()
      }?.toMap()
      EventType.TUPLET -> getTupletEvents()
      else -> super.getSpecialEvents(eventType)
    }
  }

  private fun getTupletEvents(): EventHash {
    return tuplets.flatMap { tuplet ->
      listOf(
        EMK(
          EventType.TUPLET,
          ez(0, tuplet.offset)
        ) to tuplet.toEvent().addParam(EventParam.MEMBERS to tuplet.members),
        EMK(
          EventType.TUPLET,
          ez(0, tuplet.lastMember)
        ) to tuplet.toEvent().addParam(EventParam.END, true)
      )
    }.toMap()
  }

  override fun getAllEvents(options:List<EventGetterOption>): EventHash {
    val events =
      eventMap.getAllEvents().filterNot { it.key.eventType == EventType.TIME_SIGNATURE }.toMap()
    val tupletsAsEvents =
      tuplets.map { EventMapKey(EventType.TUPLET, ez(0, it.offset)) to it.toEvent() }.toMap()
    return tupletsAsEvents.plus(events).plus(getTupletEvents(EventType.DURATION))
      .plus(getTupletEvents(EventType.TIE))
  }

  override fun stripAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    return eZero().copy(
      offset = eventAddress.offset, graceOffset = eventAddress.graceOffset,
      id = eventAddress.id
    )
  }

  override val scoreLevelType = ScoreLevelType.VOICEMAP
  override val subLevelType = ScoreLevelType.NONE

  override fun badgeEventAddress(eventAddress: EventAddress, levelIdx: Int): EventAddress {
    return eventAddress
  }

  fun getVoiceEvents(): VoiceEventMap {
    return toVoiceEvents(getEvents(EventType.DURATION) ?: eventHashOf())
  }

  open fun replaceVoiceEvents(allEvents: EventHash): VoiceMap {
    val newTuplets = mutableListOf<Tuplet>()

    val sanitised = allEvents.map {
      it.key.copy(
        eventAddress = eZero().copy(
          offset = it.key.eventAddress.offset,
          graceOffset = it.key.eventAddress.graceOffset
        )
      ) to it.value
    }.toMap()

    tuplets.forEach { tuplet ->
      val forThisTuplet = sanitised.filter { it.value.subType != DurationType.TUPLET_MARKER &&
          it.key.eventAddress.offset >= tuplet.offset &&
      it.key.eventAddress.offset < tuplet.offset + tuplet.realDuration }
      val adjusted = forThisTuplet.map { it.key.copy(eventAddress = it.key.eventAddress.copy(
        offset = tuplet.realToWritten(it.key.eventAddress.offset - tuplet.offset)
      )) to it.value }.toMap()
      val newTuplet = tuplet.replaceVoiceEvents(adjusted)
      newTuplets.add(newTuplet)
    }

    val tupletAddresses = getTupletEvents(EventType.DURATION).map { it.key.eventAddress }.toSet()
    var remainder = sanitised.filterNot { tupletAddresses.contains(it.key.eventAddress) }
    newTuplets.forEach { tuplet ->
      remainder = remainder.plus(
        EMK(
          EventType.DURATION,
          ez(0, tuplet.offset)
        ) to
            tupletMarker(tuplet.realDuration)
      )
    }
    val newMap = eventMap.replaceEvents(EventType.DURATION, remainder)
    return voiceMap(timeSignature, newMap, newTuplets)
  }

  private fun getTupletEvents(type: EventType = EventType.DURATION): EventHash {
    return tuplets.fold(eventHashOf()) { hash, tuplet ->
      tuplet.getEvents(type)?.let { hash.plus(it) } ?: hash
    }
  }

  fun getTuplet(offset: Duration): Tuplet? {
    return tuplets.find { tuplet ->
      tuplet.offset <= offset && tuplet.offset.add(tuplet.realDuration) > offset
    }
  }

  open fun addEmpties(): VoiceMap {
    val graceOther = (eventMap.getEvents(EventType.DURATION)?.toList()
      ?: listOf()).partition { it.first.eventAddress.isGrace }
    val grace = graceOther.first
    val other = graceOther.second
    val events = other.map { it.first.eventAddress.offset to it.second }.toMap().toSortedMap()
    val filled = findGaps(events, timeSignature).map { it.offset to empty(it.event.duration()) }
    events.putAll(filled)
    val hash = events.map {
      EventMapKey(
        EventType.DURATION,
        ez(0, it.key)
      ) to it.value
    }.toMap().plus(grace)
    val newMap = eventMap.replaceEvents(EventType.DURATION, hash)
    val tuplets = tuplets.map { it.addEmpties() }
    return voiceMap(timeSignature, newMap, tuplets)
  }

}

internal fun addTies(eventMap: EventMap): EventMap {
  var em = eventMap.deleteAll(EventType.TIE)
  eventMap.getEvents(EventType.DURATION)?.let { durationEvents ->
    durationEvents.forEach { (k, v) ->
      if (v.subType == DurationType.CHORD) {
        chord(v)?.let { chord ->
          chord.notes.withIndex().forEach { iv ->
            val event = when {
              iv.value.isStartTie -> Event(
                EventType.TIE,
                paramMapOf(EventParam.DURATION to chord.duration)
              )
              iv.value.isEndTie -> Event(
                EventType.TIE,
                paramMapOf(EventParam.IS_END_TIE to true, EventParam.DURATION to chord.duration)
              )
              else -> null
            }
            event?.let { em = em.putEvent(k.eventAddress.copy(id = iv.index + 1), it) }
          }
        }
      }
    }
  }
  return em
}

fun voiceMap(eventMap: EventMap, tuplets: List<Tuplet>): VoiceMap {
  val ts =
    eventMap.getEvent(EventType.TIME_SIGNATURE)?.let { timeSignature(it) } ?: TimeSignature(4, 4)
  return voiceMap(ts, eventMap, tuplets)
}

fun voiceMap(
  timeSignature: TimeSignature = TimeSignature(4, 4),
  eventMap: EventMap = emptyEventMap(),
  tuplets: List<Tuplet> = listOf(),
): VoiceMap {
  val durationEvents = eventMap.getEvents(EventType.DURATION) ?: eventHashOf()

  var map = eventMap.replaceEvents(EventType.DURATION, durationEvents)
  map = addTies(map)
  map = map.putEvent(eZero(), timeSignature.toEvent())
  return VoiceMap(timeSignature, map, tuplets)
}

fun findGaps(eventList: SortedMap<Duration, Event>, timeSignature: TimeSignature): EventList {
  if (eventList.isEmpty()) {
    return listOf()
  }

  val withStart = if (eventList.firstKey() != dZero()) {
    eventList.plus(dZero() to empty(dZero())).toSortedMap()
  } else {
    eventList
  }
  val terminated = withStart.plus(timeSignature.duration to empty(dZero()))
  val shortfalls = terminated.toList().windowed(2).mapNotNull {
    val current = it.first()
    val next = it.last()
    getGap(current.first.add(current.second.duration()), next.first)
  }
  return shortfalls.toList()
}

private fun getGap(end: Duration, nextOffset: Duration): EventListKey? {
  return if (end < nextOffset) {
    EventListKey(end, rest(nextOffset.subtract(end)))
  } else {
    null
  }
}

fun toVoiceEvents(eventHash: EventHash): VoiceEventMap {
  return eventHash.mapNotNull { entry ->
    entry.key.eventAddress.offset to entry.value
  }.toMap().toSortedMap()
}



