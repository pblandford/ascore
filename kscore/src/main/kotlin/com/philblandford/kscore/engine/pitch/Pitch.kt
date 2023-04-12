package com.philblandford.kscore.engine.pitch

import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.Accidental.*
import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.engine.types.NoteLetter
import com.philblandford.kscore.engine.types.NoteLetter.*
import com.philblandford.kscore.engine.types.Pitch


fun Pitch.getMidiVal(): Int? {
  return getMidiModulo(noteLetter, accidental)?.let { (octave + 1) * 12 + it }
}

fun noteLetterDiff(noteLetter: NoteLetter, octave: Int, steps: Int): Pair<NoteLetter, Int> {

  val numNotes = NoteLetter.values().count()
  val newPos = (noteLetter.ordinal + steps + numNotes * 10) % numNotes

  val octaveShift =
    when {
      noteLetter.ordinal + (steps % numNotes) >= numNotes -> 1
      noteLetter.ordinal + (steps % numNotes) < 0 -> -1
      else -> 0
    }
  val newOctave = octave + octaveShift + (steps / numNotes)
  return Pair(NoteLetter.values()[newPos], newOctave)
}


fun Pitch.enharmonic(accidental: Accidental):Pitch {
  return getPitchFromMidiVal(midiVal, accidental)
}

fun Pitch.getNoteShift(shift: Int, preferred: Accidental = NATURAL): Pitch {
  val newMidiVal = midiVal + shift
  return getPitchFromMidiVal(newMidiVal, preferred)
}

fun Pitch.transposeNote(shift: Int, preferred: Accidental): Pitch {
  return getNoteShift(shift, preferred)
}

fun Pitch.pitchAtStep(steps: Int, sharps: Int, up:Boolean = true, accidentalOpt: Accidental? = null): Pitch {
  val noteLetter = noteLetter.increment(steps)
  val accidental = accidentalOpt ?: getAccidental(noteLetter, sharps)
  val diff = if (up) this.noteLetter.ordinal + steps else (this.noteLetter.ordinal + 7) - steps
  val awkwardDown = noteLetter == C && accidental == FLAT
  val awkwardUp = noteLetter == B && accidental == SHARP
  var octavesCrossed =
    if (diff >= 0) diff / 7
    else (diff - 7) / 7
  if (awkwardDown) octavesCrossed -= 1
  else if (awkwardUp) octavesCrossed += 1
  return Pitch(noteLetter, accidental, octave + octavesCrossed)
}

fun Pitch.pitchesInScale(sharps: Int): List<Pitch> {
  return (0..6).map { pitchAtStep(it, sharps) }
}

fun Pitch.getScale(minor:Boolean = false):List<Pitch> {
  val ks = getSharps(this, minor) ?: 0
  return pitchesInScale(ks)
}

fun getPitchFromMidiVal(
  midiVal: Int,
  preferredAccidental: Accidental,
  octaveShift: Int = 0
): Pitch {
  val midi = midiVal + (octaveShift * 12)
  val octave = midi / 12 - 1
  val noteLetter = NoteLetter.values()[getStep(midi % 12, preferredAccidental)]
  val newMidiVal = Pitch(noteLetter, NATURAL, octave).midiVal
  return if (newMidiVal != midi) {
    getBlackKeyValue(midi, noteLetter, octave, preferredAccidental)
  } else {
    getWhiteKeyValue(noteLetter, octave, preferredAccidental)
  }
}

private fun adjustToAppearance(pitch: Pitch): Pitch {
  return when (Pair(pitch.noteLetter, pitch.accidental)) {
    Pair(B, SHARP) -> Pitch(pitch.noteLetter, pitch.accidental, pitch.octave - 1)
    Pair(C, FLAT) -> Pitch(pitch.noteLetter, pitch.accidental, pitch.octave + 1)
    else -> pitch
  }
}

fun getNotePosition(topNote: Pitch, note: Pitch): Int {
  val adjusted = adjustToAppearance(note)
  return (topNote.octave - adjusted.octave) * NoteLetter.values().size + topNote.noteLetter.ordinal -
      adjusted.noteLetter.ordinal
}

fun positionToPitch(position: Int, clefType: ClefType): Pitch? {
  return getClef(clefType)?.let { clef ->
    Pitch(clef.topNote, NATURAL, clef.topNoteOctave).pitchAtStep( -position, 0)
  }
}

fun pitchToPosition(pitch: Pitch, clefType: ClefType): Int {
  return getClef(clefType)?.let { clef ->
    getNotePosition(Pitch(clef.topNote, NATURAL, clef.topNoteOctave), pitch)
  } ?: 0
}

private fun getBlackKeyValue(
  midiVal: Int,
  noteLetter: NoteLetter,
  octave: Int,
  preferred: Accidental
): Pitch {
  return when (preferred) {
    DOUBLE_SHARP ->
      if (!isWhite(midiVal)) {
        Pitch(noteLetter, SHARP, octave)
      } else {
        val pitch = noteLetterDiff(noteLetter, octave, 2)
        Pitch(pitch.first, DOUBLE_SHARP, pitch.second)
      }
    DOUBLE_FLAT ->
      if (!isWhite(midiVal)) {
        Pitch(noteLetter, FLAT, octave)
      } else {
        Pitch(noteLetter, DOUBLE_FLAT, octave)
      }
    NATURAL -> Pitch(noteLetter, SHARP, octave)
    else -> {
      val accidental = when (preferred) {
        FORCE_SHARP -> SHARP
        FORCE_FLAT -> FLAT
        else -> preferred
      }
      Pitch(noteLetter, accidental, octave)
    }
  }
}

private fun getWhiteKeyValue(noteLetter: NoteLetter, octave: Int, preferred: Accidental): Pitch {
  return when (preferred) {
    DOUBLE_SHARP ->
      if (noteLetter == F || noteLetter == C) {
        Pitch(noteLetter, NATURAL, octave)
      } else {
        val diff = noteLetterDiff(noteLetter, octave, -1)
        Pitch(diff.first, preferred, diff.second)
      }
    DOUBLE_FLAT ->
      if (noteLetter == E || noteLetter == B) {
        Pitch(noteLetter, NATURAL, octave)
      } else {
        val diff = noteLetterDiff(noteLetter, octave, 1)
        Pitch(diff.first, preferred, diff.second)
      }
    FORCE_SHARP ->
      when (noteLetter) {
        C -> {
          val diff = noteLetterDiff(noteLetter, octave, -1)
          Pitch(diff.first, SHARP, diff.second + 1)
        }
        F -> {
          val diff = noteLetterDiff(noteLetter, octave, -1)
          Pitch(diff.first, SHARP, diff.second)
        }
        else -> Pitch(noteLetter, NATURAL, octave)
      }
    FORCE_FLAT ->
      when (noteLetter) {
        E -> {
          val diff = noteLetterDiff(noteLetter, octave, 1)
          Pitch(diff.first, FLAT, diff.second)
        }
        B -> {

          val diff = noteLetterDiff(noteLetter, octave, 1)
          Pitch(diff.first, FLAT, diff.second - 1)
        }
        else -> Pitch(noteLetter, NATURAL, octave)
      }
    else -> Pitch(noteLetter, NATURAL, octave)
  }
}


fun getStep(modulo: Int, accidental: Accidental): Int {
  return if (isUpwards(accidental)) upSteps[modulo] else downSteps[modulo]
}

private fun isUpwards(accidental: Accidental): Boolean {
  return setOf(SHARP, DOUBLE_SHARP, NATURAL, FORCE_SHARP).contains(accidental)

}

fun getMidiModulo(noteLetter: NoteLetter, accidental: Accidental): Int? {
  return modulos[Pair(noteLetter, accidental)]
}


fun isWhite(midiVal: Int): Boolean = whiteBlack[midiVal % 12]
val whiteBlack =
  arrayOf(true, false, true, false, true, true, false, true, false, true, false, true)
val upSteps = arrayOf(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6)
val downSteps = arrayOf(0, 1, 1, 2, 2, 3, 4, 4, 5, 5, 6, 6)
val plainModulos = mapOf(
  Pair(B, SHARP) to 0,
  Pair(C, NATURAL) to 0,
  Pair(D, DOUBLE_FLAT) to 0,
  Pair(C, SHARP) to 1,
  Pair(D, FLAT) to 1,
  Pair(C, DOUBLE_SHARP) to 2,
  Pair(D, NATURAL) to 2,
  Pair(E, DOUBLE_FLAT) to 2,
  Pair(D, SHARP) to 3,
  Pair(E, FLAT) to 3,
  Pair(D, DOUBLE_SHARP) to 4,
  Pair(E, NATURAL) to 4,
  Pair(F, FLAT) to 4,
  Pair(E, SHARP) to 5,
  Pair(F, NATURAL) to 5,
  Pair(G, DOUBLE_FLAT) to 5,
  Pair(F, SHARP) to 6,
  Pair(G, FLAT) to 6,
  Pair(F, DOUBLE_SHARP) to 7,
  Pair(G, NATURAL) to 7,
  Pair(A, DOUBLE_FLAT) to 7,
  Pair(G, SHARP) to 8,
  Pair(A, FLAT) to 8,
  Pair(G, DOUBLE_SHARP) to 9,
  Pair(A, NATURAL) to 9,
  Pair(B, DOUBLE_FLAT) to 9,
  Pair(A, SHARP) to 10,
  Pair(B, FLAT) to 10,
  Pair(A, DOUBLE_SHARP) to 11,
  Pair(B, NATURAL) to 11,
  Pair(C, FLAT) to 11
)

val forceSharpModulos =
  plainModulos.filter { it.key.second == SHARP }.map { Pair(it.key.first, FORCE_SHARP) to it.value }
    .toMap()
val forceFlatModulos =
  plainModulos.filter { it.key.second == FLAT }.map { Pair(it.key.first, FORCE_FLAT) to it.value }
    .toMap()

val modulos = plainModulos.plus(forceFlatModulos).plus(forceSharpModulos)