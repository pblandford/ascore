package com.philblandford.kscore.engine.pitch

import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.Accidental.*
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.NoteLetter.*
import com.philblandford.kscore.engine.types.Pitch

data class Transposition(val currentSharps: Int, val shift: Int, val accidental: Accidental? = null)

fun Event.transpose(transposition: Transposition): Event {
  return chord(this)?.let {
    Transposer.transposeChord(
      it, transposition.currentSharps, transposition.shift,
      transposition.accidental
    ).toEvent()
  } ?: this
}

object Transposer {

  fun transposeChord(
    chord: Chord, currentSharps: Int, shift: Int,
    preferredAccidental: Accidental? = null
  ): Chord {
    return if (shift != 0 || differentSharps(currentSharps, preferredAccidental)) {
      val noteList = chord.notes.map { k ->
        transposeNote(
          k, currentSharps,
          shift, preferredAccidental
        )
      }
      val newChord = chord.copy(notes = noteList)
      newChord
    } else {
      chord
    }
  }

  private fun differentSharps(currentSharps: Int, preferredAccidental: Accidental?): Boolean {
    return when (preferredAccidental) {
      SHARP -> currentSharps < 0
      else -> currentSharps > 0
    }
  }


  private fun transposeNote(
    pitchedNote: Note, currentSharps: Int, shift: Int,
    preferSharpOpt: Accidental?
  ): Note {

    val newPitch = transposePitch(pitchedNote.pitch, currentSharps, shift, preferSharpOpt)

    return pitchedNote.copy(pitch = newPitch)
  }

  fun transposeHarmony(
    harmony: Harmony, currentSharps: Int, shift: Int,
    preferredSharp: Accidental? = null
  ): Harmony {
    val root =
      harmony.root?.let { transposePitch(harmony.root, currentSharps, shift, preferredSharp) }
    val tone = transposePitch(harmony.tone, currentSharps, shift, preferredSharp)
    return harmony.copy(tone = tone, root = root)
  }

  fun transposePitch(
    pitch: Pitch,
    currentSharps: Int,
    shift: Int,
    preferSharpOpt: Accidental? = null
  ): Pitch {
    val newKeySharps = transposeKey(currentSharps, shift, preferSharpOpt)
    return transposePitch(pitch, currentSharps, newKeySharps, shift, preferSharpOpt)
  }

  private fun transposePitch(
    pitch: Pitch, currentSharps: Int, newKeySharps: Int, shift: Int,
    preferSharpOpt: Accidental?
  ): Pitch {
    val posInScale = pitch.noteLetter.diff(sSharpsToNotes[currentSharps]?.noteLetter!!)
    val alteration =
      pitch.accidental.ordinal - getAccidental(pitch.noteLetter, currentSharps).ordinal
    val scaleStart = sSharpsToNotes[newKeySharps]?.copy(octave = pitch.octave)!!
    var newPitch = scaleStart.pitchAtStep(posInScale, newKeySharps, shift > 0)

    newPitch = getNewAccidental(alteration, newPitch, newKeySharps)
    newPitch = changeIncorrectPitch(newPitch)
    val octaveShift = getOctaveShift(pitch, newPitch, shift > 0)
    val newOctave = pitch.octave + octaveShift
    newPitch = newPitch.copy(octave = newOctave)
    return newPitch.copy(showAccidental = pitch.showAccidental)
  }

  private fun getOctaveShift(old: Pitch, new: Pitch, up: Boolean): Int {
    return if (!up && old.noteLetter.ordinal - new.noteLetter.ordinal < 0 &&
      new.octaveless() != Pitch(B, SHARP).octaveless() && old.octaveless() != Pitch(C, FLAT).octaveless()) {
      -1
    } else if (up && new.noteLetter.ordinal - old.noteLetter.ordinal < 0 &&
      new.octaveless() != Pitch(C, FLAT).octaveless() && old.octaveless() != Pitch(B, SHARP).octaveless()) {
      1
    } else if (up && new.noteLetter == B && new.accidental == SHARP) {
      1
    } else if (!up && old.noteLetter == B && old.accidental == SHARP) {
      -1
    } else if (!up && new.noteLetter == C && new.accidental == FLAT) {
      -1
    } else if (up && old.noteLetter == C && old.accidental == FLAT) {
      1
    } else {
      0
    }
  }

  private fun getNewAccidental(alteration: Int, newPitch: Pitch, newKS: Int): Pitch {
    val newOrdinal = newPitch.accidental.ordinal + alteration
    return if (newOrdinal < DOUBLE_FLAT.ordinal) {
      val pitch = newPitch.copy(accidental = FLAT)
      pitch.getNoteShift(-2, if (isSharp(newKS)) SHARP else FLAT)
    } else {
      val accidental = Accidental.values()[(newPitch.accidental.ordinal + alteration) % 7]
      newPitch.copy(accidental = accidental)
    }
  }

  private fun changeIncorrectPitch(pitch: Pitch): Pitch {
    return when (pitch.noteLetter to pitch.accidental) {
      C to DOUBLE_FLAT -> Pitch(B, FLAT, pitch.octave)
      F to DOUBLE_FLAT -> Pitch(E, FLAT, pitch.octave)
      B to DOUBLE_SHARP -> Pitch(C, SHARP, pitch.octave)
      E to DOUBLE_SHARP -> Pitch(F, SHARP, pitch.octave)
      else -> pitch
    }
  }


}
