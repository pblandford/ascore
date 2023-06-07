package com.philblandford.ascore.external.export.mxml.out.creator.measure

import BeamPos
import BeamStateQuery
import com.philblandford.kscore.engine.core.score.Tuplet
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EventList
import com.philblandford.kscore.engine.pitch.positionToPitch
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.util.highestBit
import org.philblandford.ascore2.external.export.mxml.out.MxmlActualNotes
import org.philblandford.ascore2.external.export.mxml.out.MxmlAlter
import org.philblandford.ascore2.external.export.mxml.out.MxmlBeam
import org.philblandford.ascore2.external.export.mxml.out.MxmlChord
import org.philblandford.ascore2.external.export.mxml.out.MxmlDisplayOctave
import org.philblandford.ascore2.external.export.mxml.out.MxmlDisplayStep
import org.philblandford.ascore2.external.export.mxml.out.MxmlDot
import org.philblandford.ascore2.external.export.mxml.out.MxmlDuration
import org.philblandford.ascore2.external.export.mxml.out.MxmlForward
import org.philblandford.ascore2.external.export.mxml.out.MxmlGrace
import org.philblandford.ascore2.external.export.mxml.out.MxmlInstrument
import org.philblandford.ascore2.external.export.mxml.out.MxmlLyric
import org.philblandford.ascore2.external.export.mxml.out.MxmlMeasureElement
import org.philblandford.ascore2.external.export.mxml.out.MxmlNormalNotes
import org.philblandford.ascore2.external.export.mxml.out.MxmlNote
import org.philblandford.ascore2.external.export.mxml.out.MxmlNoteDescriptor
import org.philblandford.ascore2.external.export.mxml.out.MxmlNotehead
import org.philblandford.ascore2.external.export.mxml.out.MxmlOctave
import org.philblandford.ascore2.external.export.mxml.out.MxmlPitch
import org.philblandford.ascore2.external.export.mxml.out.MxmlRest
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaff
import org.philblandford.ascore2.external.export.mxml.out.MxmlStep
import org.philblandford.ascore2.external.export.mxml.out.MxmlSyllabic
import org.philblandford.ascore2.external.export.mxml.out.MxmlText
import org.philblandford.ascore2.external.export.mxml.out.MxmlTie
import org.philblandford.ascore2.external.export.mxml.out.MxmlTimeModification
import org.philblandford.ascore2.external.export.mxml.out.MxmlType
import org.philblandford.ascore2.external.export.mxml.out.MxmlUnpitched
import org.philblandford.ascore2.external.export.mxml.out.MxmlVoice
import org.philblandford.ascore2.external.export.mxml.out.accidentalToMxml
import org.philblandford.ascore2.external.export.mxml.out.durationToMxml


internal fun createNotesForEvent(
  event: Event, divisions: Int, voice: Int, staff: Int?,
  lyrics: List<Event>, tuplet: Tuplet?,
  graceEvents: EventList,
  eventAddress: EventAddress,
  instrumentId: String?,
  beamStateQuery: BeamStateQuery?,
  scoreQuery: ScoreQuery
): List<MxmlMeasureElement>? {

  val graceNotes = createNotesGrace(graceEvents, voice, staff, scoreQuery)

  val mainEvents = when (event.subType) {
    DurationType.CHORD -> createNotesChord(
      event,
      divisions,
      voice,
      staff,
      false,
      lyrics,
      tuplet,
      eventAddress,
      instrumentId,
      beamStateQuery,
      scoreQuery
    )
    DurationType.REST -> listOf(
      createNote(
        event, event, divisions, MxmlRest(), false, false, voice, staff, listOf(), tuplet,
        eventAddress, instrumentId, null, scoreQuery
      )
    )
    DurationType.EMPTY -> listOf(createEmpty(event, divisions))
    else -> listOf()
  }
  return mainEvents?.let {
    (graceNotes ?: listOf()).plus(mainEvents)
  }
}

internal fun Duration.toMxml(divisions: Int): Int {
  return multiply(4).multiply(divisions).toInt()
}

private fun createNotesGrace(
  graceEvents: EventList, voice: Int, staff: Int?,
  scoreQuery: ScoreQuery
): List<MxmlNote>? {
  return graceEvents.flatMap { (k, v) ->
    createNotesChord(
      v, null, voice, staff,
      true, listOf(), null, k.eventAddress, null, null, scoreQuery
    ) ?: listOf()
  }
}

private fun createNotesChord(
  chordEvent: Event, divisions: Int?, voice: Int, staff: Int?,
  isGrace: Boolean,
  lyrics: List<Event>, tuplet: Tuplet?, eventAddress: EventAddress,
  instrumentId: String?,
  beamStateQuery: BeamStateQuery?,
  scoreQuery: ScoreQuery
): List<MxmlNote>? {
  return chord(chordEvent)?.let { chord ->
    chord.notes.withIndex().map {
      createNote(
        it.value.toEvent(),
        chordEvent,
        divisions,
        createPitch(it.value.pitch, it.value.position.y),
        it.index != 0,
        isGrace,
        voice,
        staff,
        lyrics,
        tuplet,
        eventAddress,
        instrumentId,
        beamStateQuery,
        scoreQuery
      )
    }
  }
}

private fun createEmpty(event: Event, divisions: Int): MxmlForward {
  val duration = event.duration().toMxml(divisions)
  return MxmlForward(MxmlDuration(duration))
}

private fun createNote(
  note: Event,
  chord: Event,
  divisions: Int?,
  descriptor: MxmlNoteDescriptor,
  isChord: Boolean,
  isGrace: Boolean,
  voice: Int,
  staff: Int?,
  lyrics: List<Event>,
  tuplet: Tuplet?,
  eventAddress: EventAddress,
  instrumentId: String?,
  beamStateQuery: BeamStateQuery?,
  scoreQuery: ScoreQuery
): MxmlNote {
  val duration = divisions?.let { note.realDuration().toMxml(divisions) }
  val dots = (1..note.duration().numDots()).map { MxmlDot() }
  val type = getMxmlType(note.duration(), scoreQuery.isEmptyBar(eventAddress))
  val tie = createTie(note)
  val instrument = createInstrument(note, instrumentId, eventAddress)
  val timeModification = createTimeModification(note, tuplet)
  val notations = createNotations(note, chord, eventAddress, scoreQuery, !isChord)
  val mxmlLyrics = lyrics.mapNotNull { createLyric(it) }
  val notehead = createNotehead(note)
  val beams = createBeams(beamStateQuery, eventAddress.offset, isChord)
  return MxmlNote(
    if (isGrace) MxmlGrace() else null,
    if (isChord) MxmlChord() else null,
    descriptor,
    duration?.let { MxmlDuration(it) },
    tie,
    instrument,
    MxmlVoice(voice),
    type,
    dots,
    timeModification,
    notehead,
    staff?.let { MxmlStaff(it) },
    beams,
    notations,
    mxmlLyrics
  )
}

private fun createNotehead(note: Event): MxmlNotehead? {
  return when (note.getParam<NoteHeadType>(EventParam.NOTE_HEAD_TYPE)) {
    NoteHeadType.CROSS -> MxmlNotehead("x")
    NoteHeadType.DIAMOND -> MxmlNotehead("diamond")
    else -> null
  }
}

private fun createPitch(pitch: Pitch, position: Int): MxmlNoteDescriptor {
  return if (pitch == unPitched()) {
    createUnpitched(position)
  } else {
    MxmlPitch(
      MxmlStep(pitch.noteLetter.toString()),
      accidentalToMxml(pitch.accidental)?.let { MxmlAlter(it) },
      MxmlOctave(pitch.getOctave())
    )
  }
}

private fun Pitch.getOctave(): Int {
  return if (noteLetter == NoteLetter.B && accidental == Accidental.SHARP) {
    octave - 1
  } else if (noteLetter == NoteLetter.C && accidental == Accidental.FLAT) {
    octave + 1
  } else {
    octave
  }
}

private fun createInstrument(
  note: Event,
  instrumentId: String?,
  eventAddress: EventAddress
): MxmlInstrument? {
  return note.getParam<Pitch>(EventParam.PITCH)?.let { pitch ->
    if (pitch == unPitched()) {
      note.getParam<Int>(EventParam.MIDIVAL)?.let { midiVal ->
        MxmlInstrument("P${eventAddress.staveId.main}-I$midiVal")
      }
    } else {
      instrumentId?.let { MxmlInstrument(it) }
    }
  }
}

private fun createUnpitched(position: Int): MxmlUnpitched {
  return positionToPitch(position, ClefType.TREBLE)?.let { pitch ->
    MxmlUnpitched(
      MxmlDisplayStep(pitch.noteLetter.toString()),
      MxmlDisplayOctave(pitch.octave)
    )
  } ?: MxmlUnpitched(MxmlDisplayStep("C"), MxmlDisplayOctave(4))
}

private fun createTie(note: Event): MxmlTie? {
  return if (note.isTrue(EventParam.IS_START_TIE) && note.isTrue(EventParam.IS_END_TIE)) {
    MxmlTie("continue")
  } else if (note.isTrue(EventParam.IS_START_TIE)) {
    MxmlTie("start")
  } else if (note.isTrue(EventParam.IS_END_TIE)) {
    MxmlTie("stop")
  } else null
}

private fun createLyric(lyric: Event?): MxmlLyric? {
  return lyric?.let {
    val number = lyric.getParam<Int>(EventParam.NUMBER) ?: 1

    val type = lyric.getParam<LyricType>(EventParam.TYPE) ?: LyricType.END
    val syllabic = when (type) {
      LyricType.MID -> "middle"
      LyricType.END -> "end"
      LyricType.START -> "begin"
      LyricType.ALL -> "all"
    }
    lyric.getParam<String>(EventParam.TEXT)?.let { text ->
      MxmlLyric(number, MxmlSyllabic(syllabic), MxmlText(text))
    }
  }
}

private fun createBeams(
  beamStateQuery: BeamStateQuery?,
  offset: Offset,
  chord: Boolean
): List<MxmlBeam> {
  if (chord) {
    return listOf()
  }
  return beamStateQuery?.getState(offset)?.let { beamStates ->
    beamStates.map { beamState ->
      val number = beamState.duration.denominator.highestBit() - 3
      val text = when (beamState.beamPos) {
        BeamPos.START -> "begin"
        BeamPos.MID -> "continue"
        BeamPos.END -> "end"
      }
      MxmlBeam(number, text)
    }
  } ?: listOf()
}

private fun createTimeModification(note: Event, tuplet: Tuplet?): MxmlTimeModification? {

  if (note.duration() != note.realDuration()) {
    val ratio = note.duration().divide(note.realDuration())
    val tupletNumerator = tuplet?.timeSignature?.numerator ?: ratio.numerator
    val mult = tupletNumerator / ratio.numerator

    return MxmlTimeModification(
      MxmlActualNotes(ratio.numerator * mult),
      MxmlNormalNotes(ratio.denominator * mult)
    )
  }
  return null
}

internal fun getMxmlType(duration: Duration, emptyBar: Boolean): MxmlType? {
  if (emptyBar) {
    return null
  }
  val text = durationToMxml(duration) ?: "whole"
  return MxmlType(text)
}
