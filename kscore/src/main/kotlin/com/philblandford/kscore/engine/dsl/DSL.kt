package com.philblandford.kscore.engine.dsl

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.defaultInstrument
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.*
import com.philblandford.kscore.engine.eventadder.NewEventAdder
import com.philblandford.kscore.engine.eventadder.rightOrThrow
import com.philblandford.kscore.engine.eventadder.util.getStemDirection
import com.philblandford.kscore.engine.eventadder.util.getYPosition
import com.philblandford.kscore.engine.eventadder.util.setXPositions
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*


fun score(
  sharps: Int = 0, timeSignature: TimeSignature = TimeSignature(4, 4),
  tempo: Tempo = Tempo(crotchet(), 120), meta: Meta = Meta(), pageSize: PageSize = PageSize.A5,
  block: ScoreBuilder.() -> Unit
): Score = ScoreBuilder(sharps, timeSignature, tempo, meta, pageSize).apply(block).build()

class ScoreBuilder(
  val sharps: Int, val timeSignature: TimeSignature, val tempo: Tempo,
  val meta: Meta, pageSize: PageSize
) {
  var parts: List<Part> = listOf()
  var events: EventMap = initEvents(pageSize)

  fun tempoText(text: String, address: EventAddress) {
    events =
      events.putEvent(address, Event(EventType.TEMPO_TEXT, paramMapOf(EventParam.TEXT to text)))
  }

  fun repeat(start: Boolean, address: EventAddress) {
    val type = if (start) EventType.REPEAT_START else EventType.REPEAT_END
    events = events.putEvent(address, Event(type, paramMapOf()))
  }

  fun part(instrument: Instrument = defaultInstrument(), block: PartBuilder.() -> Unit) {
    parts = parts + dslPart({ timeSignature }, instrument, block)
  }

  fun build(): Score {
    val score = Score(
      parts.toList(),
      events.putEvent(
        ez(1),
        Event(EventType.KEY_SIGNATURE, paramMapOf(EventParam.SHARPS to sharps))
      ).putEvent(ez(1), timeSignature.toEvent()).putEvent(ez(1), tempo.toEvent())
    )
    val metaEvent = meta.toEvent()
    return NewEventAdder.addEvent(score, metaEvent.eventType, metaEvent.params, eZero())
      .rightOrThrow()
  }
}

fun dslPart(
  getTimeSignature: (Int) -> TimeSignature = { TimeSignature(4, 4) },
  instrument: Instrument = defaultInstrument(),
  block: PartBuilder.() -> Unit
): Part = PartBuilder(getTimeSignature, instrument).apply(block).build()

class PartBuilder(
  private val getTimeSignature: (Int) -> TimeSignature,
  val instrument: Instrument = defaultInstrument()
) {
  private var staves: List<Stave> = listOf()

  fun stave(clef: ClefType = ClefType.TREBLE, block: StaveBuilder.() -> Unit) {
    staves = staves + dslStave(getTimeSignature, clef, block)
  }

  fun build(): Part {
    val events = emptyEventMap()
      .putEvent(ez(1), instrument.toEvent())
    return part(if (staves.isEmpty()) listOf(Stave()) else staves.toList(), events)
  }
}

fun dslStave(
  getTimeSignature: (Int) -> TimeSignature,
  clef: ClefType = ClefType.TREBLE,
  block: StaveBuilder.() -> Unit
): Stave = StaveBuilder(getTimeSignature, clef).apply(block).build()

class StaveBuilder(
  val getTimeSignature: (Int) -> TimeSignature,
  clefType: ClefType = ClefType.TREBLE
) {
  private var bars: List<Bar> = listOf()
  private var barNum: Int = 1
  private var clef: ClefType = ClefType.TREBLE

  var events: EventMap = emptyEventMap().putEvent(
    ez(1),
    Event(EventType.CLEF, paramMapOf(EventParam.TYPE to clefType))
  )

  fun clef(type: ClefType, address: EventAddress) {
    events = events.putEvent(
      address.copy(staveId = sZero()),
      Event(EventType.CLEF, paramMapOf(EventParam.TYPE to type))
    )
    clef = type
  }

  fun bar(block: BarBuilder.() -> Unit) {
    bars = bars + dslBar(getTimeSignature(barNum), { clef }, block)
    barNum++
  }

  fun build(): Stave = Stave(bars.toList(), events)
}


fun dslBar(
  timeSignature: TimeSignature = TimeSignature(4, 4),
  getClef: (Duration) -> ClefType = { ClefType.TREBLE },
  block: BarBuilder.() -> Unit
): Bar = BarBuilder(timeSignature, getClef).apply(block).build()

class BarBuilder(
  val timeSignature: TimeSignature,
  val getClef: (Duration) -> ClefType
) {
  var voiceMaps: List<VoiceMap> = listOf()
  var eventMap = emptyEventMap()

  fun voiceMap(block: VoiceMapBuilder.() -> Unit) {
    voiceMaps = voiceMaps + (dslVoiceMap(timeSignature, getClef, block))
  }

  fun event(event: Event) {
     eventMap = eventMap.putEvent(eZero(), event)
  }

  fun build(): Bar = Bar(timeSignature, voiceMaps.toList(), eventMap)
}

fun dslVoiceMap(
  timeSignature: TimeSignature = TimeSignature(4, 4),
  getClef: (Duration) -> ClefType = { ClefType.TREBLE },
  block: VoiceMapBuilder.() -> Unit
): VoiceMap = VoiceMapBuilder(
  timeSignature,
  getClef
).apply(block).build()

class VoiceMapBuilder(val timeSignature: TimeSignature, val getClef: (Duration) -> ClefType) {
  private var eventHash: EventHash =
    eventHashOf()
  private var offset: Duration = dZero()
  private var currentDuration = crotchet()
  private var tuplets = listOf<Tuplet>()

  fun chord(
    duration: Duration = crotchet(), up: Boolean? = null, ornamentType: OrnamentType? = null,
    block: ChordBuilder.() -> Unit = { pitch(NoteLetter.F) }
  ) {
    eventHash = eventHash.plus(
      EventMapKey(
        EventType.DURATION,
        ez(0, offset)
      ) to
          dslChord(duration, getClef(offset), up, ornamentType, block)
    )
    offset = offset.add(duration)
  }

  fun rest(duration: Duration = crotchet()) {
    eventHash = eventHash.plus(
      EventMapKey(
        EventType.DURATION,
        ez(0, offset)
      ) to
          Event(
            EventType.DURATION,
            DurationType.REST,
            paramMapOf(EventParam.DURATION to duration)
          )
    )
    offset = offset.add(duration)
  }

  fun tuplet(numerator: Int, denominator: Int, voiceMap: VoiceMap) {
    val tuplet = tuplet(offset, numerator, denominator, false, voiceMap.eventMap)
    tuplets += tuplet
  }

  fun toQuaver() {
    currentDuration = quaver()
  }

  fun toCrotchet() {
    currentDuration = crotchet()
  }

  fun toMinim() {
    currentDuration = minim()
  }

  fun note(
    noteLetter: NoteLetter = NoteLetter.F, octave: Int = 4, duration: Duration = currentDuration,
    accidental: Accidental = Accidental.NATURAL, up: Boolean? = null,
    ornamentType: OrnamentType? = null, showAccidental: Boolean? = null
  ) {
    chord(duration, up, ornamentType) {
      pitch(
        noteLetter, octave = octave, accidental = accidental,
        showAccidental = showAccidental ?: false
      )
    }
  }

  fun build(): VoiceMap = voiceMap(
    timeSignature,
    eventMapOf(eventHash),
    tuplets
  )
}

fun dslChord(
  duration: Duration = crotchet(), clefType: ClefType = ClefType.TREBLE,
  up: Boolean? = null, ornamentType: OrnamentType? = null,
  block: ChordBuilder.() -> Unit = { pitch(NoteLetter.F) }
): Event =
  ChordBuilder(duration, clefType, up, ornamentType).apply(block).build()

class ChordBuilder(
  val duration: Duration, private val clefType: ClefType,
  private val up: Boolean? = null, private val ornamentType: OrnamentType? = null
) {
  var pitches: List<Pitch> = listOf()

  fun pitch(
    noteLetter: NoteLetter, accidental: Accidental = Accidental.NATURAL, octave: Int = 4,
    showAccidental: Boolean = false
  ) {
    pitches = pitches + Pitch(noteLetter, accidental, octave, showAccidental)
  }

  fun build(): Event {
    val noteEvents = pitches.map {
      note(it, duration, clefType)
    }
    val upStem =
      up ?: getStemDirection(
        noteEvents.mapNotNull { it.params.g<Coord>(EventParam.POSITION) }, null,
        false
      )

    var event =
      Chord(duration, noteEvents.mapNotNull { note(it) }, Modifiable(false, upStem)).toEvent()
    ornamentType?.let {
      event = event.addParam(EventParam.ORNAMENT, it)
    }
    event = setXPositions(event)
    return event
  }
}

fun note(pitch: Pitch, duration: Duration, clefType: ClefType = ClefType.TREBLE): Event {
  val position = getYPosition(pitch, clefType) ?: Coord()
  return Note(duration, pitch, position = position).toEvent()
}

fun note(
  noteLetter: NoteLetter,
  duration: Duration = crotchet(),
  clefType: ClefType = ClefType.TREBLE
): Event {
  val pitch = Pitch(noteLetter)
  val position = getYPosition(pitch, clefType) ?: Coord()
  return Note(duration, pitch, position = position).toEvent()
}

fun rest(duration: Duration = crotchet(), realDuration: Duration = duration): Event {
  return Event(
    EventType.DURATION, paramMapOf(
      EventParam.TYPE to DurationType.REST,
      EventParam.DURATION to duration, EventParam.REAL_DURATION to realDuration
    )
  )
}

fun empty(duration: Duration): Event {
  return Event(
    EventType.DURATION,
    paramMapOf(EventParam.TYPE to DurationType.EMPTY, EventParam.DURATION to duration)
  )
}

fun tupletMarker(duration: Duration): Event {
  return Event(
    EventType.DURATION,
    paramMapOf(
      EventParam.TYPE to DurationType.TUPLET_MARKER, EventParam.DURATION to duration,
      EventParam.REAL_DURATION to duration
    )
  )
}