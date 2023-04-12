package com.philblandford.kscore.sound

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLogv
import java.util.*


interface MidiPlayLookup {
  fun getEvents(ms: MS): Iterable<MidiEvent>?
  fun getEvents(offset: Offset): Iterable<MidiEvent>?
  fun getMetaEvents(): Map<Offset, Iterable<MetaEvent>>
  fun msToAddress(ms: MS): EventAddress?
  fun addressToMs(eventAddress: EventAddress): MS?
  fun lastMs(): MS
  fun getProgramEvents(ms: MS?): Iterable<ProgramChangeEvent>
  fun allOffsets(): Iterable<Offset>
  fun channelToStave(channel: Channel): StaveId
}

typealias MsEventMap = Map<MS, Iterable<MidiEvent>>
typealias MsEventMapM = MutableMap<MS, MutableList<MidiEvent>>
typealias MetaEventMap = Map<Offset, Iterable<MetaEvent>>
typealias MS = Int
typealias Channel = Int

data class NoteAndDuration(
  val midiVal: Int, val velocity: Int, val start: MS, val duration: MS,
  val original: Note
)

fun midiPlayLookup(
  scoreQuery: ScoreQuery,
  instrumentGetter: InstrumentGetter,
  start: EventAddress? = null,
  end: EventAddress? = null
): MidiPlayLookup {
  ksLogv("Creating midi builder")
  val midiBuilder = midiBuilder(scoreQuery, instrumentGetter, start, end)
  ksLogv("Creating play lookup")

  return createMidiPlayLookup(midiBuilder, instrumentGetter)
}

internal fun createMidiPlayLookup(midiBuilder: MidiBuilder, instrumentGetter: InstrumentGetter): MidiPlayLookup {

  val msLookup = midiBuilder.msLookup()

  val map = mutableMapOf<Int, MutableList<MidiEvent>>()
  msLookup.forEach {
    it.key to putEventsAtAddress(it.key, it.value, midiBuilder, map, instrumentGetter)
  }

  val sorted = map.map { (ms, events) ->
    ms to events.sortedBy { it.priority }
  }.toMap().toSortedMap()

  val metaEvents = createMetaEvents(midiBuilder)

  return MidiPlayLookUpImpl(sorted, metaEvents, midiBuilder)
}

private class MidiPlayLookUpImpl(
  val msEventMap: MsEventMap,
  val metaEventMap: MetaEventMap,
  val midiBuilder: MidiBuilder
) : MidiPlayLookup {
  private val lastMsVal = msEventMap.keys.maxOrNull() ?: 0

  override fun getEvents(ms: Int): Iterable<MidiEvent>? {
    return msEventMap[ms]
  }

  override fun getEvents(offset: Offset): Iterable<MidiEvent>? {
    return midiBuilder.offsetToMs(offset)?.let {
      msEventMap[it]
    }
  }

  override fun msToAddress(ms: Int): EventAddress? {
    return midiBuilder.msLookup()[ms]?.let { offset ->
      midiBuilder.offsetToAddress(offset)
    }
  }

  override fun addressToMs(eventAddress: EventAddress): MS? {
    return midiBuilder.addressToOffset(eventAddress)?.let {
      midiBuilder.offsetToMs(it)
    }
  }

  override fun lastMs(): MS {
    return lastMsVal
  }

  override fun getProgramEvents(ms: MS?): Iterable<ProgramChangeEvent> {
    val upTo = ms?.let {  msEventMap.toList().takeWhile { it.first <= ms } } ?: msEventMap.toList()
    val map = mutableMapOf<Channel, ProgramChangeEvent>()
    upTo.forEach { (_, v) ->
      v.filterIsInstance<ProgramChangeEvent>().forEach { pce ->
        map[pce.channel] = pce
      }
    }
    return map.values
  }

  override fun allOffsets(): Iterable<Offset> {
    return midiBuilder.allOffsets()
  }

  override fun channelToStave(channel: Channel): StaveId {
    return midiBuilder.channelToStave(channel)
  }

  override fun getMetaEvents(): Map<Offset, Iterable<MetaEvent>> {
    return metaEventMap
  }
}

private fun putEventsAtAddress(
  ms: Int,
  offset: Offset,
  midiBuilder: MidiBuilder,
  msEventMap: MsEventMapM,
  instrumentGetter: InstrumentGetter
) {
  midiBuilder.getEvents(offset).forEach { event ->
    putMidiEventsForScoreEvent(event, ms, offset, midiBuilder, msEventMap, instrumentGetter)
  }
}

private fun putMidiEventsForScoreEvent(
  event: ChannelEvent,
  ms: Int,
  offset: Offset,
  midiBuilder: MidiBuilder,
  msEventMap: MsEventMapM,
  instrumentGetter: InstrumentGetter
) {
  when (event.event.eventType) {
    EventType.DURATION -> putDurationEvents(event, ms, offset, midiBuilder, msEventMap)
    EventType.INSTRUMENT -> putInstrumentEvent(event, ms, msEventMap)
    EventType.PEDAL -> putPedalEvent(event, ms, msEventMap)
    EventType.EXPRESSION_TEXT -> putExpressionEvent(event, ms, msEventMap, instrumentGetter)
    else -> {
    }
  }
}

private fun putInstrumentEvent(
  event: ChannelEvent,
  ms: Int,
  msEventMap: MsEventMapM
) {
  val program = event.event.getInt(EventParam.PROGRAM)
  val soundFont = event.event.getString(EventParam.SOUNDFONT)
  val bank = event.event.getInt(EventParam.BANK)
  addToMap(
    ms, ProgramChangeEvent(
      program, event.channel,
      soundFont, bank
    ), msEventMap
  )
}

private fun putExpressionEvent(
  event: ChannelEvent, ms: Int, msEventMap: MsEventMapM,
  instrumentGetter: InstrumentGetter
) {
  event.event.getParam<String>(EventParam.TEXT)?.let { text ->
    findInstrument(text, instrumentGetter)?.let { instrument ->
      addToMap(
        ms, ProgramChangeEvent(
          instrument.program, event.channel,
          instrument.soundFont, instrument.bank
        ), msEventMap)
    }
  }
}

private fun findInstrument(
  text: String,
  instrumentGetter: InstrumentGetter
): Instrument? {
  return when (text.toLowerCase(Locale.getDefault()).dropLastWhile { !it.isLetter() }) {
    "pizz" -> instrumentGetter.getInstrument("Pizzicato Strings")
    "arco" -> null
    "mute" -> instrumentGetter.getInstrument("Muted Trumpet")
    "muted" -> instrumentGetter.getInstrument("Muted Trumpet")
    "open" -> instrumentGetter.getInstrument("Trumpet")
    else -> instrumentGetter.getInstrument(text)
  }
}

private fun putPedalEvent(
  event: ChannelEvent,
  ms: Int,
  msEventMap: MsEventMapM
) {
  val on = !event.event.isTrue(EventParam.END)
  addToMap(ms, PedalEvent(event.channel, on), msEventMap)
}

private fun putDurationEvents(
  event: ChannelEvent,
  ms: Int,
  offset: Offset,
  midiBuilder: MidiBuilder,
  msEventMap: MsEventMapM
) {
  midiBuilder.getEndEvent(offset, event.event)?.let { end ->
    chord(event.event)?.let { chord ->
      val velocity = midiBuilder.getVelocity(event.channel, offset)
      val chordNotes = chord.notes.mapNotNull { note ->
        midiBuilder.getEndEvent(offset, note.toEvent())?.let { noteEnd ->
          NoteAndDuration(
            midiBuilder.getMidiVal(event.channel, note.getMidi()), velocity, 0, noteEnd - ms, note
          )
        }
      }
      val transformed = chordNotes.filterNot { it.original.isEndTie }
      if (transformed.count() == 0) {
        addEmptyToMap(ms, msEventMap)
      } else {
        transformed.forEach { note ->
          val on = NoteOnEvent(note.midiVal, note.velocity, event.channel)
          val off = NoteOffEvent(on.midiVal, event.channel)
          addToMap(ms + note.start, on, msEventMap)
          addToMap(ms + note.start + note.duration, off, msEventMap)
        }
      }
    } ?: run {
      addToMap(ms, null, msEventMap)
    }
  }
}

private fun Note.getMidi(): Int {
  return if (percussion) percussionId else {
    pitch.midiVal
  }
}

private fun addEmptyToMap(key: Int, map: MutableMap<Int, MutableList<MidiEvent>>) {
  val list = map[key] ?: mutableListOf()
  map[key] = list
}

private fun addToMap(key: Int, value: MidiEvent?, map: MutableMap<Int, MutableList<MidiEvent>>) {
  val list = map[key] ?: mutableListOf()
  value?.let { list.add(it) }
  map[key] = list
}

private fun createMetaEvents(midiBuilder: MidiBuilder): MetaEventMap {
  return midiBuilder.getMetaEvents().toList()
    .groupBy { Pair(it.first.eventAddress.barNum, it.first.eventAddress.offset) }
    .mapNotNull { (key, events) ->
      midiBuilder.addressToOffset(ez(key.first, key.second))?.let { offset ->
        offset to events.mapNotNull { it.second.toMetaEvent() }
      }
    }.toMap()
}

private fun Event.toMetaEvent(): MetaEvent? {
  return when (eventType) {
    EventType.TIME_SIGNATURE -> toTimeSignature()
    EventType.TEMPO -> toTempo()
    EventType.KEY_SIGNATURE -> toKeySignature()
    else -> null
  }
}

private fun Event.toTimeSignature(): TimeSignatureEvent? {
  return getParam<Int>(EventParam.NUMERATOR)?.let { num ->
    getParam<Int>(EventParam.DENOMINATOR)?.let { den ->
      TimeSignatureEvent(num, den)
    }
  }
}

private fun Event.toTempo(): TempoEvent? {
  return getParam<Int>(EventParam.BPM)?.let { bpm ->
    return getParam<Duration>(EventParam.DURATION)?.let { duration ->
      TempoEvent(bpm, duration)
    }
  }
}

private fun Event.toKeySignature(): KeySignatureEvent? {
  return getParam<Int>(EventParam.SHARPS)?.let { sharps ->
    KeySignatureEvent(sharps)
  }
}