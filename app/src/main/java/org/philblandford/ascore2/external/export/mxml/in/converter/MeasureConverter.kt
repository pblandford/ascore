package com.philblandford.ascore.external.export.mxml.`in`.converter

import com.philblandford.ascore.external.export.mxml.out.*
import com.philblandford.ascore.external.export.mxml.out.creator.RepeatBarDesc
import com.philblandford.kscore.api.PercussionDescr
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.Bar
import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.emptyEventMap
import com.philblandford.kscore.engine.newadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.newadder.util.getYPosition
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLoge
import com.philblandford.kscore.log.ksLogv
import com.philblandford.kscore.util.isPower2

internal data class OngoingAttributes(
  val divisions: Int = 1,
  val clefs: Map<StaveNum, ClefType> = mapOf(),
  val timeSignature: TimeSignature = TimeSignature(4, 4),
  val keySignature: Int = 0,
  val staves: Int = 1,
  val staffLines: Int = 5,
  val transpose: Int = 0,
  val octaveShifts: Map<StaveNum, Int> = mapOf(),
  val instrumentId: String? = null,
  val repeatBars: Map<StaveNum, Pair<BarNum, RepeatBarDesc>> = mapOf()
)

internal data class MeasureReturn(
  val bars: Map<Int, Bar>, val attributes: OngoingAttributes,
  val scoreEvents: EventMap = emptyEventMap(),
  val partEvents: EventMap = emptyEventMap(),
  val staveEvents: EventMap = emptyEventMap(),
  val percussionStaves: Map<String, PercussionDescr> = mapOf()
)

internal data class MeasureState(
  val current: EventAddress,
  val next: EventAddress,
  val events: EventMap,
  val attributes: OngoingAttributes,
  val scoreEvents: EventMap = emptyEventMap(),
  val partEvents: EventMap = emptyEventMap(),
  val staveEvents: EventMap = emptyEventMap(),
  val barEvents: EventMap = emptyEventMap(),
  val percussionStaves: Map<String, PercussionDescr> = mapOf(),
  val tupletStart: Offset? = null,
  val tupletEnd: Offset? = null
)

internal data class ChordDecorationState(
  val articulations: Iterable<ArticulationType> = listOf(),
  val fingerings: Iterable<Int> = listOf(),
  val tremolo: Duration? = null,
  val bowing: BowingType? = null,
  val ornament: Ornament? = null,
  val arpeggio: ArpeggioType? = null
)

internal data class VMKey(val staff: Int, val voice: Int)

internal fun mxmlMeasureToBar(
  mxmlMeasure: MxmlMeasure,
  mxmlScorePart: MxmlScorePart,
  ongoingAttributes: OngoingAttributes,
  num: Int,
  getInstrument: (String) -> Event?
): MeasureReturn? {

  ksLogv("Converting measure $num")

  val measureState = convertMeasure(mxmlMeasure, mxmlScorePart, ongoingAttributes, getInstrument)
  val bars = createBars(measureState)

  return MeasureReturn(
    bars, measureState.attributes, measureState.scoreEvents, measureState.partEvents,
    measureState.staveEvents, measureState.percussionStaves
  )
}

private fun convertMeasure(
  mxmlMeasure: MxmlMeasure, mxmlScorePart: MxmlScorePart,
  ongoingAttributes: OngoingAttributes,
  getInstrument: (String) -> Event?
): MeasureState {
  var measureState = MeasureState(
    eav(0), eav(0),
    emptyEventMap(), ongoingAttributes
  )

  measureState = mxmlMeasure.elements.fold(measureState) { ms, elem ->
    convertElement(elem, mxmlScorePart, ms, getInstrument)
  }
  measureState = addRepeatBar(measureState)

  return measureState
}

private fun addRepeatBar(measureState: MeasureState): MeasureState {
  return measureState.attributes.repeatBars.toList().fold(measureState) { ms, (staff, pair) ->
    val bar = pair.first
    val desc = pair.second
    if (desc.num == 2 && bar % 2 == 0) {
      ms
    } else {

      val staveEvents = ms.staveEvents.putEvent(
        eZero().copy(staveId = StaveId(0, staff)),
        Event(EventType.REPEAT_BAR, paramMapOf(EventParam.NUMBER to desc.num))
      )
      val repeats =
        if (desc.start) ms.attributes.repeatBars else ms.attributes.repeatBars.minus(staff)
      ms.copy(staveEvents = staveEvents, attributes = ms.attributes.copy(repeatBars = repeats))
    }
  }
}

private fun convertElement(
  element: MxmlMeasureElement,
  mxmlScorePart: MxmlScorePart,
  measureState: MeasureState,
  getInstrument: (String) -> Event?
): MeasureState {

  return when (element) {
    is MxmlNote -> convertNote(element, mxmlScorePart, measureState, getInstrument)
    is MxmlAttributes -> convertAttributes(element, measureState, mxmlScorePart)
    is MxmlBackup -> convertBackup(element, measureState)
    is MxmlForward -> convertForward(element, measureState)
    is MxmlDirection -> convertDirection(element, measureState)
    is MxmlBarline -> convertBarLine(element, measureState)
    is MxmlHarmony -> convertHarmony(element, measureState)
    is MxmlPrint -> convertPrint(element, measureState)
    else -> measureState
  }
}

private fun convertNote(
  mxmlNote: MxmlNote, mxmlScorePart: MxmlScorePart,
  measureState: MeasureState,
  getInstrument: (String) -> Event?
): MeasureState {

  return mxmlToNote(
    mxmlNote,
    mxmlScorePart,
    measureState
  )?.let { noteConverterReturn ->
    var ms = addNote(
      noteConverterReturn.note, mxmlNote,
      noteConverterReturn.chordDecorationState, noteConverterReturn.measureState
    )
    ms = addLyrics(noteConverterReturn.lyrics, ms)
    ms = addInstrument(ms, measureState, getInstrument)
    ms
  } ?: measureState
}

private fun addInstrument(
  newMeasureState: MeasureState,
  measureState: MeasureState,
  getInstrument: (String) -> Event?
): MeasureState {
  return newMeasureState.attributes.instrumentId?.let { id ->
    measureState.attributes.instrumentId?.let { oldId ->
      if (id != oldId) {
        getInstrument(id)?.let { event ->
          val staveEvents = newMeasureState.staveEvents.putEvent(newMeasureState.current, event)
          newMeasureState.copy(staveEvents = staveEvents)
        }
      } else newMeasureState
    }
  } ?: newMeasureState
}

private fun positionNote(note: Note, attributes: OngoingAttributes, staff: Int): Note {
  return attributes.clefs[staff]?.let { clef ->
    if (clef == ClefType.PERCUSSION) {
      note
    } else {
      val octave = attributes.octaveShifts[staff] ?: 0
      val pos = getYPosition(note.pitch, clef, -octave) ?: Coord()
      note.copy(position = pos)
    }
  } ?: note
}

private fun addNote(
  noteEvent: Event, mxmlNote: MxmlNote,
  chordDecorationState: ChordDecorationState,
  measureState: MeasureState
): MeasureState {

  val staff = mxmlNote.staff?.num ?: 1
  val current = measureState.current.copy(staveId = StaveId(0, staff), voice = mxmlNote.voice.num)
  val next = measureState.next.copy(staveId = StaveId(0, staff), voice = mxmlNote.voice.num)


  return if (mxmlNote.chord != null) {
    measureState.events.getEvent(EventType.DURATION, current)?.let { chordEvent ->
      chord(chordEvent)?.let { chord ->
        note(noteEvent)?.let { note ->
          val positioned = positionNote(note, measureState.attributes, staff)
          val newEvents = measureState.events.putEvent(current, chord.addNote(positioned).toEvent())
          measureState.copy(events = newEvents)
        }
      }
    } ?: measureState
  } else {
    val graceOffset = if (mxmlNote.grace != null) next.graceOffset ?: dZero() else null
    val graceNext = next.copy(graceOffset = graceOffset)
    var newEvents = if (noteEvent.subType == DurationType.REST) {
      measureState.events.putEvent(next, noteEvent)
    } else {
      note(noteEvent)?.let { note ->
        val positioned = positionNote(note, measureState.attributes, staff)
        val chord = createChord(positioned, chordDecorationState)
        measureState.events.putEvent(graceNext, chord)
      } ?: measureState.events
    }
    val pair = addTuplet(mxmlNote, noteEvent.realDuration(), next, measureState, newEvents)
    newEvents = pair.first
    val newState = pair.second
    val nextGrace = graceOffset?.addC(noteEvent.realDuration())
    val nextOffset =
      if (graceOffset == null) graceNext.offset.addC(noteEvent.realDuration()) else graceNext.offset
    val newNext = graceNext.copy(offset = nextOffset, graceOffset = nextGrace)

    newState.copy(current = measureState.next, next = newNext, events = newEvents)
  }
}

private fun addLyrics(lyrics: Map<Int, Event>, measureState: MeasureState): MeasureState {
  val events = lyrics.toList().fold(measureState.events) { em, ly ->
    em.putEvent(measureState.current.copy(id = ly.first), ly.second)
  }
  return measureState.copy(events = events)
}

private fun convertBackup(mxmlBackup: MxmlBackup, measureState: MeasureState): MeasureState {
  val duration = getDuration(mxmlBackup.duration.num, measureState.attributes.divisions)
  val newOffset = measureState.next.offset.subtract(duration)
  val newAddress = measureState.next.copy(offset = newOffset)
  return measureState.copy(current = newAddress, next = newAddress)
}

private fun convertForward(mxmlForward: MxmlForward, measureState: MeasureState): MeasureState {
  val duration = getDuration(mxmlForward.duration.num, measureState.attributes.divisions)
  val newOffset = measureState.next.offset.addC(duration)
  val newAddress = measureState.next.copy(offset = newOffset)
  return measureState.copy(current = newAddress, next = newAddress)
}


private fun addTuplet(
  element: MxmlNote, noteDuration: Duration, eventAddress: EventAddress,
  measureState: MeasureState,
  events: EventMap
): Pair<EventMap, MeasureState> {
  measureState.tupletEnd?.let { end ->
    if (end == eventAddress.offset) {
      measureState.tupletStart?.let { start ->


        element.timeModification?.let {

          val tupletDuration = fixTuplet(end.subtract(start).add(noteDuration))
          if (dodgyTuplet(tupletDuration)) {
            ksLoge("Suspicious tuplet $tupletDuration")
            return Pair(events, measureState)
          }
          val tuplet = tuplet(eventAddress.offset, it.actualNotes.num, tupletDuration)

          return Pair(
            events.putEvent(eventAddress.copy(offset = start), tuplet.toEvent()),
            measureState.copy(tupletStart = null, tupletEnd = null)
          )
        }
      }
    }
  }
  return Pair(events, measureState)
}

internal fun getDenominator(tupletDuration: Duration, normalNotes: Int, actualNotes: Int): Int {
  return (tupletDuration.denominator * normalNotes) / tupletDuration.numerator
}

private fun fixTuplet(duration: Duration): Duration {
  if (dodgyTuplet(duration)) {
    val added = Duration(duration.numerator + 1, duration.denominator)
    if (added.denominator.isPower2()) {
      return added
    } else {
      return Duration(duration.numerator - 1, duration.denominator)
    }
  } else return duration
}

private fun dodgyTuplet(duration: Duration): Boolean {
  return duration == dZero() || !duration.denominator.isPower2()
}

private fun createChord(
  note: Note,
  chordDecorationState: ChordDecorationState
): Event {
  var chord = Chord(note.duration, listOf(note), realDuration = note.realDuration).toEvent()
  if (chordDecorationState.articulations.count() != 0) {
    chord = chord.addParam(
      EventParam.ARTICULATION to ChordDecoration(items = chordDecorationState.articulations)
    )
  }
  if (chordDecorationState.fingerings.count() != 0) {
    chord =
      chord.addParam(
        EventParam.FINGERING to ChordDecoration(
          items = chordDecorationState.fingerings
        )
      )
  }
  chordDecorationState.ornament?.let { ornament ->
    chord = chord.addParam(EventParam.ORNAMENT to ChordDecoration(items = listOf(ornament)))
  }
  chordDecorationState.arpeggio?.let { arpeggioType ->
    chord = chord.addParam(EventParam.ARPEGGIO to ChordDecoration(items =listOf(arpeggioType)))
  }
  chordDecorationState.tremolo?.let { tremolo ->
    chord = chord.addParam(EventParam.TREMOLO_BEATS to ChordDecoration(items = listOf(tremolo)))
  }
  return chord
}


private fun convertBarLine(mxmlBarline: MxmlBarline, measureState: MeasureState): MeasureState {

  val event = mxmlBarline.repeat?.let {
    if (it.direction == "forward") {
      Event(EventType.REPEAT_START)
    } else {
      Event(EventType.REPEAT_END)
    }
  } ?: mxmlBarline.barStyle?.text?.let { barStyle ->
    val type = when (barStyle) {
      "regular" -> BarLineType.NORMAL
      "light-light" -> BarLineType.DOUBLE
      "light-heavy" -> BarLineType.FINAL
      else -> BarLineType.NORMAL
    }
    Event(EventType.BARLINE, paramMapOf(EventParam.TYPE to type))
  }

  var em = measureState.scoreEvents
  event?.let {
    if (event.subType != BarLineType.NORMAL) {
      em = em.putEvent(eZero(), event)
    }
  }
  mxmlBarline.ending?.let { ending ->
    em = getEnding(ending, em)
  }
  return measureState.copy(scoreEvents = em)
}

private fun getEnding(mxmlEnding: MxmlEnding, eventMap: EventMap): EventMap {

  var event = Event(EventType.VOLTA, paramMapOf(EventParam.NUMBER to mxmlEnding.number))
  if (mxmlEnding.type != "start") {
    event = event.addParam(EventParam.END, true)
  }
  return eventMap.putEvent(eZero(), event)
}

private fun convertPrint(
  mxmlPrint: MxmlPrint,
  measureState: MeasureState
): MeasureState {
  return mxmlPrint.newSystem?.let { _ ->
    val em = measureState.scoreEvents.putEvent(
      eZero(),
      Event(EventType.BREAK, paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    )
    measureState.copy(scoreEvents = em)
  } ?: measureState
}