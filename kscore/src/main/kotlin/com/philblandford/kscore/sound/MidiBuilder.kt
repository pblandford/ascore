package com.philblandford.kscore.sound

import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.pitch.harmony
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.time.timeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLoge
import com.philblandford.kscore.log.ksLogv
import com.philblandford.kscore.option.getOption
import com.philblandford.kscore.sound.transform.Transformer
import kotlinx.coroutines.runBlocking
import kotlin.math.absoluteValue
import kotlin.math.sign

interface MidiBuilder {
  fun msLookup(): MsLookup
  fun offsetToMs(offset: Offset): Int?
  fun offsetToAddress(offset: Offset): EventAddress?
  fun addressToOffset(eventAddress: EventAddress): Offset?
  fun getEvents(offset: Offset): Iterable<ChannelEvent>
  fun getMetaEvents(): EventHash
  fun getEndEvent(offset: Offset, event: Event): Int?
  fun getMidiVal(channel: Int, original: Int): Int
  fun getVelocity(channel: Int, offset: Offset): Int
  fun allOffsets(): Iterable<Offset>
  fun longTrillActive(channel: Int, offset: Offset): Event?
  fun channelToStave(channel: Channel): StaveId
}

data class ChannelEvent(val channel: Int, val event: Event)
typealias EMSimple = Map<EventType, Iterable<ChannelEvent>>
typealias MsLookup = Map<Int, Offset>
typealias EventLookup = Map<Offset, EMSimple>


internal data class Horizontal(val barNum: Int = 1, val offset: Offset = dZero()) :
  Comparable<Horizontal> {
  override fun compareTo(other: Horizontal): Int {
    var ret = barNum - other.barNum
    if (ret == 0) ret = offset.compareTo(other.offset)
    return ret
  }
}

private class MidiBuilderImpl(
  val scoreQuery: ScoreQuery, val instrumentGetter: InstrumentGetter, start: EventAddress?,
  endExcl: EventAddress?
) : MidiBuilder {

  val longTrillGetter = MidiLongTrillGetter(scoreQuery)

  private val allEvents =
    getAllEvents(scoreQuery, start, endExcl, instrumentGetter, longTrillGetter)

  val barList = midiBarList(scoreQuery, start?.barNum, endExcl?.barNum)
  val othMap = offsetToHorizontalMap(barList, allEvents)
  val htoMap = othMap.toList().sortedBy { dZero() - it.first }.map { it.second to it.first }.toMap()
  val staveToChannelMap = createChannels(scoreQuery)
  val channelToPartMap = staveToChannelMap.map { it.value to it.key }.toMap()
  val eventLookup: EventLookup = eventLookup(othMap, allEvents, staveToChannelMap, scoreQuery)

  init {
    checkEventLookup(eventLookup, start, endExcl)
  }

  val msLookup = midiMsLookup(eventLookup)
  val msReverseLookup = msLookup.map { it.value to it.key }.toMap()
  val transposeMap = getTransposeMap(scoreQuery, staveToChannelMap)
  val dynamicGetter = MidiDynamicGetter(scoreQuery)
  val metaEventHash = collectMetaEvents(scoreQuery)

  override fun msLookup(): MsLookup {
    return msLookup
  }

  override fun getEvents(offset: Offset): Iterable<ChannelEvent> {
    return eventLookup[offset]?.flatMap { it.value } ?: listOf()
  }

  override fun getEndEvent(offset: Offset, event: Event): Int? {
    val duration = event.realDuration()
    return msReverseLookup[offset.addC(duration)]
  }

  override fun getMidiVal(channel: Int, original: Int): Int {
    return transposeMap[channel]?.let {
      original + it
    } ?: original
  }

  override fun getVelocity(channel: Int, offset: Offset): Int {
    return othMap[offset]?.let { h ->
      dynamicGetter.getVelocity(
        eas(
          h.barNum, h.offset,
          channelToStave(channel)
        )
      )
    } ?: 100
  }

  override fun longTrillActive(channel: Int, offset: Offset): Event? {
    return othMap[offset]?.let { h ->
      longTrillGetter.longTrillActive(eas(h.barNum, h.offset, StaveId(getPart(channel), 1)))
    }
  }

  override fun offsetToMs(offset: Offset): Int? {
    return msReverseLookup[offset]
  }

  override fun offsetToAddress(offset: Offset): EventAddress? {
    return othMap[offset]?.let { ez(it.barNum, it.offset) }
  }

  override fun addressToOffset(eventAddress: EventAddress): Offset? {
    return htoMap[hz(eventAddress.barNum, eventAddress.offset)]
  }

  override fun allOffsets(): Iterable<Offset> {
    return msReverseLookup.keys
  }

  override fun channelToStave(channel: Channel): StaveId {
    return channelToPartMap[channel] ?: StaveId(1, 1)
  }

  override fun getMetaEvents(): EventHash {
    return metaEventHash
  }
}

private fun transformDurationEvents(
  eventHash: EventHash,
  longTrillGetter: MidiLongTrillGetter?,
  scoreQuery: ScoreQuery
): EventHash {

  val getNextChord = { ea: EventAddress ->
    scoreQuery.getEvent(EventType.GLISSANDO, ea.voiceIdless())?.let {
      if (it.isTrue(EventParam.END)) {
        null
      } else {
        scoreQuery.getNextStaveSegment(ea)?.let { next ->
          scoreQuery.getEvent(EventType.DURATION, next.copy(voice = ea.voice))?.let { nextChord ->
            chord(nextChord)
          }
        }
      }
    }
  }

  val partitioned = eventHash.toList().partition { it.first.eventAddress.isGrace }
  val graceGrouped =
    partitioned.first.groupBy { it.first.eventAddress.graceless() }
  val notGrace = partitioned.second
  return runBlocking {
    notGrace.flatMap { (k, ev) ->
      val ea = k.eventAddress
      val lt = longTrillGetter?.longTrillActive(ea)
      val ks = scoreQuery.getKeySignature(ea) ?: 0
      val grace =
        graceGrouped[ea.graceless()]?.mapNotNull { (gk, v) ->
          chord(v)?.let { gk.eventAddress.graceOffset!! to it }
        }?.toMap() ?: mapOf()
      Transformer.transform(ev, ks, lt, grace)
      { getNextChord(ea) }.withIndex().map { iv ->
        k.copy(
          eventAddress = ea.copy(
            offset = ea.offset + iv.value.first,
            id = iv.index
          )
        ) to iv.value.second
      }.toList()
    }.toMap()
  }
}

internal fun getAllEvents(
  scoreQuery: ScoreQuery,
  start: EventAddress?,
  endExcl: EventAddress?,
  instrumentGetter: InstrumentGetter,
  longTrillGetter: MidiLongTrillGetter? = null
): EventHash {
  val durationEvents = getDurationEvents(start, endExcl, longTrillGetter, scoreQuery)
  var allEvents = addGeneralEvents(start, endExcl, scoreQuery)
  allEvents = addSystemEvents(start, endExcl, allEvents, scoreQuery)
  allEvents = addHarmonyEvents(start, endExcl, allEvents, scoreQuery, instrumentGetter)
  allEvents = addExpressionEvents(start, endExcl, allEvents, scoreQuery)

  return durationEvents.plus(allEvents)
}

private fun getDurationEvents(
  start: EventAddress?, endExcl: EventAddress?,
  longTrillGetter: MidiLongTrillGetter?,
  scoreQuery: ScoreQuery
): EventHash {
  var empty = scoreQuery.getEmptyVoiceMaps().map {
    EMK(EventType.DURATION, it.copy(voice = 1)) to rest(semibreve())
  }

  val all = scoreQuery.getEvents(EventType.DURATION, start?.copy(graceOffset = dZero()), endExcl)
    ?.let { hash ->
      val notFinal =
        endExcl?.let { end -> hash.filterNot { it.key.eventAddress.horizontal == end.horizontal } }
          ?: hash
      transformDurationEvents(notFinal, longTrillGetter, scoreQuery)
    } ?: mapOf()
  val lastEvent = all.maxByOrNull { it.key.eventAddress }?.key?.eventAddress ?: eZero()
  val numParts = scoreQuery.allParts(true).count()
  empty = empty.groupBy { it.first.eventAddress.barNum }.filter { it.value.size == numParts }
    .map { it.value.first() }
    .sortedBy { it.first.eventAddress }
    .dropLastWhile { it.first.eventAddress.barNum > lastEvent.barNum }
  return empty.fold(all) { m, (k, v) ->
    m.plus(Pair(k, v))
  }
}

private fun addGeneralEvents(
  start: EventAddress?, endExcl: EventAddress?,
  scoreQuery: ScoreQuery
): EventHash {
  var res = scoreQuery.collateEvents(
    listOf(
      EventType.INSTRUMENT,
      EventType.PEDAL, EventType.TIME_SIGNATURE, EventType.TEMPO
    ), start, endExcl
  ) ?: eventHashOf()
  res = res.flatMap { (key, value) ->
    if (key.eventType == EventType.INSTRUMENT && key.eventAddress.isStart()) {
      (1..scoreQuery.numStaves(key.eventAddress.staveId.main)).map { stave ->
        key.copy(eventAddress = key.eventAddress.copy(staveId = StaveId(key.eventAddress.staveId.main, stave))) to value
      }
    } else {
      listOf(key to value)
    }
  }.toMap()
  return res
}

private fun addExpressionEvents(
  start: EventAddress?, endExcl: EventAddress?,
  eventHash: EventHash, scoreQuery: ScoreQuery
): EventHash {
  val staveRange = start?.let {
    val end = endExcl?.staveId ?: scoreQuery.getAllStaves(true).toList().last()
    val range = scoreQuery.getStaveRange(start.staveId, end)
    ksLoge("range $range")
    range
  } ?: scoreQuery.getAllStaves(true)
  ksLoge("rangeRet $staveRange")
  var current = staveRange.mapNotNull { staveId ->
    start?.let {
      val byId = (0..1).mapNotNull { id ->
        scoreQuery.getEventAt(EventType.EXPRESSION_TEXT, start.copy(staveId = staveId, id = id))
      }
      byId.maxByOrNull { it.first.eventAddress.horizontal }
    }
  }.toMap()
  ksLoge("range current $current")

  current = current.map {
    it.key.copy(
      eventAddress = maxOf(it.key.eventAddress, start?.copy(staveId = it.key.eventAddress.staveId) ?: it.key.eventAddress).copy(id = 0)
    ) to it.value
  }.toMap()
  ksLoge("range current2 $current")

  val inRange = scoreQuery.getEvents(EventType.EXPRESSION_TEXT, start, endExcl) ?: mapOf()
  return eventHash + current + inRange
}

private fun addSystemEvents(
  start: EventAddress?, endExcl: EventAddress?, eventHash: EventHash,
  scoreQuery: ScoreQuery
): EventHash {
  var allEvents = eventHash
  start?.let {
    scoreQuery.getEventAt(EventType.TIME_SIGNATURE, start)?.let { (_, v) ->
      allEvents = allEvents.plus(EMK(v.eventType, ez(start.barNum)) to v)
    }
    scoreQuery.getEventAt(EventType.TEMPO, start)?.let { (_, v) ->
      allEvents = allEvents.plus(EMK(v.eventType, ez(start.barNum)) to v)
    }
    scoreQuery.getEvent(EventType.HIDDEN_TIME_SIGNATURE, start)?.let { event ->
      allEvents = allEvents.plus(EMK(event.eventType, ez(start.barNum)) to event)
    }
    (start.staveId.main..(endExcl?.staveId?.main ?: scoreQuery.numParts)).forEach { part ->
      (1..scoreQuery.numStaves(part)).forEach { stave ->
        scoreQuery.getEventAt(EventType.INSTRUMENT, start.copy(staveId = StaveId(part, stave)))
          ?.let { (_, v) ->
            allEvents =
              allEvents.plus(EMK(v.eventType, start.copy(staveId = StaveId(part, stave))) to v)
          }
      }
    }
  }
  return allEvents
}

private fun addHarmonyEvents(
  start: EventAddress?, endExcl: EventAddress?, eventHash: EventHash, scoreQuery: ScoreQuery,
  instrumentGetter: InstrumentGetter
): EventHash {
  var allEvents = eventHash
  if (getOption(EventParam.OPTION_HARMONY, scoreQuery)) {
    val stave = StaveId(scoreQuery.numParts + 1, 0)
    val chords =
      scoreQuery.getEvents(EventType.HARMONY, start, endExcl)?.mapNotNull { (key, event) ->
        harmony(event)?.let {
          createHarmonyChord(it)?.toEvent()?.let { ev ->
            key.copy(
              eventType = EventType.DURATION,
              eventAddress = key.eventAddress.copy(staveId = stave)
            ) to ev
          }
        }
      }
    chords?.let {
      allEvents = allEvents.plus(chords)
      val instrument = getOption(
        EventParam.OPTION_HARMONY_INSTRUMENT,
        scoreQuery
      ) ?: "Piano"
      instrumentGetter.getInstrument(instrument)?.let { instr ->
        val addr = (start ?: ez(1)).copy(staveId = stave)
        allEvents = allEvents.plus(EMK(EventType.INSTRUMENT, addr) to instr.toEvent())
      }
    }
  }
  return allEvents
}


internal fun offsetToHorizontalMap(
  barList: BarList,
  allEvents: EventHash
): Map<Offset, Horizontal> {
  ksLogv("Creating oth map")

  var currentTs: TimeSignature? = null
  var currentOffset: Offset? = null
  var lastBarStart: Offset? = dZero()
  val map = mutableMapOf<Offset, Horizontal>()

  val grouped = allEvents.toList().groupBy {
    Horizontal(
      it.first.eventAddress.barNum,
      it.first.eventAddress.offset
    )
  }
  val horizontals = createHorizontalList(barList, grouped.keys)

  horizontals.forEach { horizontal ->
    if (currentOffset == null) {
      currentOffset = dZero()
      lastBarStart = currentOffset
    } else if (horizontal.offset == dZero()) {
      currentOffset = lastBarStart?.addC(currentTs?.duration ?: dZero())
      lastBarStart = currentOffset
    } else {
      currentOffset = lastBarStart?.addC(horizontal.offset)
    }

    val address = ez(horizontal.barNum, horizontal.offset)
    currentOffset?.let { map.put(it, horizontal) }
    currentTs = (allEvents[EMK(EventType.HIDDEN_TIME_SIGNATURE, address)] ?: allEvents[EMK(EventType.TIME_SIGNATURE, address)])?.let { timeSignature(it) }
      ?: currentTs
  }

  return map.toMap()
}

private fun setShuffle(offset: Offset, event: Event, shuffle: Boolean): Pair<Offset, Event> {
  return if (shuffle && offset.denominator == 8 && event.realDuration() == quaver()) {
    val newEvent = chord(event)?.setDuration(
      Duration(
        1,
        12
      )
    )?.toEvent()?.addParam(EventParam.OPTION_SHUFFLE_RHYTHM, true) ?: event
    Pair(offset.add(Duration(1, 24)), newEvent)
  } else {
    Pair(offset, event)
  }
}

private fun treatEvent(event: Event, eventAddress: EventAddress, scoreQuery: ScoreQuery): Event {

  if (event.getParam<Iterable<Event>>(EventParam.NOTES)
      ?.any { it.isTrue(EventParam.IS_START_TIE) } == false
  ) {
    return event
  }

  return chord(event)?.let { chord ->
    val notes = chord.notes.withIndex().map { iv ->
      if (!iv.value.isStartTie) {
        iv.value
      } else {
        scoreQuery.getNoteDuration(eventAddress.copy(id = iv.index + 1))?.let { duration ->
          val shuffled = if (event.isTrue(EventParam.OPTION_SHUFFLE_RHYTHM)) {
            duration - Duration(1, 24)
          } else duration
          iv.value.copy(
            realDuration = shuffled
          )
        } ?: iv.value
      }
    }
    chord.copy(notes = notes).toEvent()
  } ?: event
}


internal fun getPart(channel: Int): Int {
  val part = channel + 1
  return if (part < 10) {
    part
  } else {
    part - 1
  }
}


private fun getTransposeMap(
  scoreQuery: ScoreQuery,
  staveToChannelMap: Map<StaveId, Int>
): Map<Int, Int> {
  ksLogv("Creating transpose map")

  val showConcert = getOption<Boolean>(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, scoreQuery)
  val staves = scoreQuery.getAllStaves(true)
  return staves.mapNotNull { stave ->
    scoreQuery.getParam<Int>(
      EventType.INSTRUMENT, EventParam.TRANSPOSITION,
      eas(1, dZero(), StaveId(stave.main, 0))
    )?.let { transposition ->
      if (showConcert) {
        (staveToChannelMap[stave] ?: 0) to
            if (transposition.absoluteValue > 6) 12 * transposition.sign
            else 0
      } else {
        (staveToChannelMap[stave] ?: 0) to transposition
      }
    }
  }.toMap()
}

private fun collectMetaEvents(scoreQuery: ScoreQuery): EventHash {
  return scoreQuery.collateEvents(
    listOf(
      EventType.KEY_SIGNATURE, EventType.TEMPO,
      EventType.TIME_SIGNATURE
    )
  ) ?: eventHashOf()
}

private fun checkEventLookup(
  eventLookup: EventLookup,
  start: EventAddress?,
  endExcl: EventAddress?
) {
  if (eventLookup.isEmpty()) {
    //CrashReporter.report(Exception("Empty events! $start $endExcl"))
  }
}


internal fun midiBuilder(
  scoreQuery: ScoreQuery,
  instrumentGetter: InstrumentGetter,
  start: EventAddress? = null,
  endExcl: EventAddress? = null
): MidiBuilder {
  return MidiBuilderImpl(scoreQuery, instrumentGetter, start, endExcl)
}

internal fun createHorizontalList(
  barList: BarList,
  allHorizontals: Iterable<Horizontal>
): Iterable<Horizontal> {
  val grouped = allHorizontals.toList().groupBy { it.barNum }

  return barList.flatMap { bar ->
    grouped[bar]?.mapNotNull {
      hz(
        it.barNum,
        it.offset
      )
    }?.plus(Horizontal(bar))?.distinct()?.toSortedSet()
      ?: listOf(Horizontal(bar))
  }
}

private fun hz(barNum: Int, offset: Offset): Horizontal? {
  return if (barNum > 0 && offset >= dZero()) {
    Horizontal(barNum, offset)
  } else null
}

fun createChannels(scoreQuery: ScoreQuery): Map<StaveId, Channel> {
  val staves = scoreQuery.getAllStaves(true)
  var map = staves.withIndex().map { iv ->
    val percussion = scoreQuery.getParam<Boolean>(
      EventType.INSTRUMENT, EventParam.PERCUSSION,
      eas(1, dZero(), StaveId(iv.value.main, 0))
    ) ?: false
    val channel = if (percussion) 9 else getChannel(iv.index)
    iv.value to channel
  }.toMap()

  if (getOption(EventParam.OPTION_HARMONY, scoreQuery)) {
    map = map.plus(StaveId(staves.last().main + 1, 0) to getChannel(scoreQuery.numParts))
  }
  return map
}


internal fun getChannel(part: Int): Int {
  return if (part % 16 < 9) {
    part
  } else {
    part + 1
  }
}


internal fun eventLookup(
  othMap: Map<Offset, Horizontal>, allEvents: EventHash,
  channelMap: Map<StaveId, Int>, scoreQuery: ScoreQuery
): EventLookup {

  ksLogv("Creating event lookup")

  val map = mutableMapOf<Offset, EMSimple>()
  val grouped = allEvents.toList().groupBy {
    Horizontal(
      it.first.eventAddress.barNum,
      it.first.eventAddress.offset
    )
  }

  val shuffle = getOption<Boolean>(EventParam.OPTION_SHUFFLE_RHYTHM, scoreQuery)

  othMap.forEach { (offset, horizontal) ->
    grouped[horizontal]?.let { events ->

      val shuffled = events.toList().map {
        val p = setShuffle(offset, it.second, shuffle)
        Pair(p.first, Pair(it.first, p.second))
      }.groupBy { it.first }.map { Pair(it.key, it.value.map { it.second }) }


      val simpleEvents = shuffled.map { (key, events) ->
        key to
            events.groupBy { it.second.eventType }.map { entry ->
              entry.key to entry.value.map { (key, event) ->
                ChannelEvent(
                  channelMap[key.eventAddress.staveId] ?: 0,
                  treatEvent(event, key.eventAddress, scoreQuery)
                )
              }
            }.toMap()
      }
      simpleEvents.forEach {
        map[it.first] = it.second
      }
    }
  }

  return map
}
