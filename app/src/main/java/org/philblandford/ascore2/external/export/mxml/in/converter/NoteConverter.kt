package org.philblandford.ascore2.external.export.mxml.`in`.converter

import com.philblandford.ascore.external.export.mxml.`in`.converter.ChordDecorationState
import com.philblandford.ascore.external.export.mxml.`in`.converter.MeasureState
import com.philblandford.ascore.external.export.mxml.`in`.converter.OngoingAttributes
import com.philblandford.ascore.external.export.mxml.`in`.converter.getNotations
import com.philblandford.kscore.api.PercussionDescr
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.pitch.pitchToPosition
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLogv
import com.philblandford.kscore.util.isPower2
import org.apache.commons.math3.fraction.Fraction
import org.philblandford.ascore2.external.export.mxml.out.MxmlInstrument
import org.philblandford.ascore2.external.export.mxml.out.MxmlLyric
import org.philblandford.ascore2.external.export.mxml.out.MxmlNote
import org.philblandford.ascore2.external.export.mxml.out.MxmlPitch
import org.philblandford.ascore2.external.export.mxml.out.MxmlRest
import org.philblandford.ascore2.external.export.mxml.out.MxmlScorePart
import org.philblandford.ascore2.external.export.mxml.out.MxmlUnpitched
import org.philblandford.ascore2.external.export.mxml.out.mxmlToAccidental


internal data class NoteConverterReturn(
  val note: Event, val lyrics: Map<Int, Event>,
  val measureState: MeasureState,
  val chordDecorationState: ChordDecorationState = ChordDecorationState(),
  val percussionStaves: Map<String, Int> = mapOf()
)

internal fun mxmlToNote(
  mxmlNote: MxmlNote, mxmlScorePart: MxmlScorePart,
  measureState: MeasureState
): NoteConverterReturn? {
  val lyrics = mutableMapOf<Int, Event>()

  val writtenRealDuration = getDuration(mxmlNote, measureState.attributes.divisions)

  mxmlNote.lyric.forEach { lyric ->
    lyric.convertLyric()?.let {
      lyrics.put(lyric.number ?: 1, it)
    }
  }

  val notehead = getNoteHead(mxmlNote)

  val note = when (mxmlNote.descriptor) {
    is MxmlPitch ->
      mxmlToPitch(mxmlNote.descriptor)?.let { pitch ->
        Note(
          writtenRealDuration.first, pitch,
          realDuration = writtenRealDuration.second, isSmall = mxmlNote.grace != null,
          noteHeadType = notehead
        ).toEvent()
      }
    is MxmlRest ->
      rest(writtenRealDuration.first, writtenRealDuration.second)
    is MxmlUnpitched -> {
      val staveLine = mxmlToStaveLine(mxmlNote.descriptor, mxmlScorePart, measureState.attributes)
      val midiVal = getPercussionMidiVal(mxmlNote, mxmlScorePart)
      Note(
        writtenRealDuration.first, unPitched(), noteHeadType = notehead,
        realDuration = writtenRealDuration.second,
        position = Coord(0, staveLine), percussion = true, percussionId = midiVal,
        isSmall = mxmlNote.grace != null
      ).toEvent()
    }
    else -> null
  }

  return note?.let {
    var ncr = NoteConverterReturn(note, lyrics, measureState, ChordDecorationState())
    mxmlNote.notations?.let {
      ncr = getNotations(it, measureState.next.offset, mxmlNote.staff?.num ?: 1, ncr)
    }
    mxmlNote.instrument?.let {
      val ms = markPercussionStaveLine(ncr.measureState, it, note(note))
      ncr = ncr.copy(measureState = ms)
    }
    mxmlNote.instrument?.id?.let { id ->
      ncr = ncr.copy(
        measureState = ncr.measureState.copy(
          attributes = ncr.measureState.attributes.copy(instrumentId = id)
        )
      )
    }
    ncr
  }
}

private fun getDuration(mxmlNote: MxmlNote, divisions: Int): Pair<Duration, Duration> {
  val realDuration = mxmlNote.duration?.let { getDuration(it.num, divisions) }
    ?: mxmlNote.type?.let { durationFromType(it.text, mxmlNote.dot.count()) } ?: crotchet()
  return mxmlNote.timeModification?.let {
    val ratio = Fraction(it.actualNotes.num, it.normalNotes.num)
    val written = realDuration.multiply(ratio)
    fixDuration(written, realDuration, ratio)
  } ?: Pair(realDuration, realDuration)
}

private fun durationFromType(type: String, dots: Int): Duration {
  return try {
    val den = type.dropLast(2).toInt()
    Duration(1, den).dot(dots)
  } catch (e: Exception) {
    val base = when (type) {
      "eighth" -> quaver()
      "quarter" -> crotchet()
      "half" -> minim()
      "whole" -> semibreve()
      "breve" -> breve()
      "long" -> longa()
      "maxima" -> maxima()
      else -> crotchet()
    }
    base.dot(dots)
  }


}

private fun fixDuration(
  written: Duration,
  real: Duration,
  ratio: Duration
): Pair<Duration, Duration> {
  return if (!written.denominator.isPower2()) {
    val newWritten = Duration((written.numerator / 2) * 2, written.denominator)
    val newReal = newWritten.divide(ratio)
    Pair(newWritten, newReal)
  } else {
    Pair(written, real)
  }
}


internal fun getDuration(duration: Int, divisions: Int): Duration {

  val ret = Fraction(duration).divide(4).divide(divisions)
  ksLogv("$ret ${ret.multiply(4)} $duration $divisions ${ret.numDots()}")
  return ret
}

private fun getNoteHead(mxmlNote: MxmlNote): NoteHeadType {
  return mxmlNote.notehead?.let {
    when (it.text) {
      "x" -> NoteHeadType.CROSS
      "diamond" -> NoteHeadType.DIAMOND
      else -> NoteHeadType.NORMAL
    }
  } ?: NoteHeadType.NORMAL
}


private fun mxmlToPitch(mxmlPitch: MxmlPitch): Pitch? {
  val accidental = mxmlPitch.alter?.let { mxmlToAccidental(it.num) } ?: Accidental.NATURAL
  val noteLetter = NoteLetter.valueOf(mxmlPitch.step.text)
  val octave = getOctave(noteLetter, accidental, mxmlPitch.octave.num)
  return Pitch(noteLetter, accidental, octave)
}

private fun getOctave(noteLetter: NoteLetter, accidental: Accidental, octave:Int):Int {
  return if (noteLetter == NoteLetter.C && accidental == Accidental.FLAT) {
    octave - 1
  } else if (noteLetter == NoteLetter.B && accidental == Accidental.SHARP) {
    octave + 1
  } else {
    octave
  }
}

private fun mxmlToStaveLine(
  mxmlUnpitched: MxmlUnpitched, mxmlScorePart: MxmlScorePart,
  attributes: OngoingAttributes
): Int {
  if (attributes.staffLines == 1) {
    return 4
  }
  return pitchToPosition(
    Pitch(
      NoteLetter.valueOf(mxmlUnpitched.mxmlDisplayStep.text),
      Accidental.NATURAL, mxmlUnpitched.mxmlDisplayOctave.num
    ), ClefType.TREBLE
  )
}

private fun MxmlLyric.convertLyric(): Event? {
  val num = number ?: 1
  val type = when (syllabic?.text) {
    "begin" -> LyricType.START
    "middle" -> LyricType.MID
    "end" -> LyricType.END
    else -> LyricType.ALL
  }
  return Event(
    EventType.LYRIC, paramMapOf(
      EventParam.TEXT to text.text,
      EventParam.TYPE to type,
      EventParam.NUMBER to num
    )
  )
}

private fun getPercussionMidiVal(mxmlNote: MxmlNote, mxmlScorePart: MxmlScorePart): Int {
  return mxmlNote.instrument?.let { instr ->
    mxmlScorePart.midiInstrument.find { it.id == instr.id }?.midiUnpitched?.num
  } ?: 0
}

private fun markPercussionStaveLine(
  measureState: MeasureState,
  mxmlInstrument: MxmlInstrument,
  note: Note?
): MeasureState {
  return note?.let {
    val descr = PercussionDescr(note.position.y, note.percussionId, false, "", note.noteHeadType)
    val percussionStaves = measureState.percussionStaves.plus(mxmlInstrument.id to descr)
    measureState.copy(percussionStaves = percussionStaves)
  } ?: measureState
}
