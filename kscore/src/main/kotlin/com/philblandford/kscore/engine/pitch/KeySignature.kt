package com.philblandford.kscore.engine.pitch

import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.Accidental.*
import com.philblandford.kscore.engine.types.NoteLetter.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class KeySignature(val sharps: Int) {
  fun toEvent(): Event {
    return Event(EventType.KEY_SIGNATURE, paramMapOf(EventParam.SHARPS to sharps))
  }
}

fun keySignature(event: Event): KeySignature? {
  return event.getParam<Int>(EventParam.SHARPS)?.let {
    KeySignature(it)
  }
}


val sSharps = listOf(F, C, G, D, A, E, B)
val sFlats = listOf(B, E, A, D, G, C, F)

fun getAccidental(noteLetter: NoteLetter, sharps: Int): Accidental {
  val noteLetters = if (sharps >= 0) sSharps else sFlats
  return when {
    noteLetters.indexOf(noteLetter) >= abs(sharps) -> NATURAL
    sharps >= 0 -> SHARP
    else -> FLAT
  }
}

data class AccidentalPositions(val sharpPositions: List<Int>, val flatPositions: List<Int>)

fun getAccidentalPositions(clefType: ClefType): AccidentalPositions? {
  return getClef(clefType)?.let { clef ->
    AccidentalPositions(clef.sharpPositions.toList(), clef.flatPositions.toList())
  }
}

val sNotesToSharps = mapOf(
  Pitch(C, NATURAL) to 0,
  Pitch(C, SHARP) to 7,
  Pitch(D, FLAT) to -5,
  Pitch(D, NATURAL) to 2,
  Pitch(E, FLAT) to -3,
  Pitch(E, NATURAL) to 4,
  Pitch(F, NATURAL) to -1,
  Pitch(F, SHARP) to 6,
  Pitch(G, FLAT) to -6,
  Pitch(G, NATURAL) to 1,
  Pitch(A, FLAT) to -4,
  Pitch(A, NATURAL) to 3,
  Pitch(B, FLAT) to -2,
  Pitch(B, NATURAL) to 5,
  Pitch(C, FLAT) to -7
)
val sSharpsToNotes = sNotesToSharps.map { it.value to it.key }.toMap()

fun isSharp(ks: Int) = ks >= 0

fun getSharps(pitch: Pitch, minor: Boolean = false): Int? {
  val actual = if (minor) {
    val accidental = if (pitch.octaveless() == Pitch(D, SHARP).octaveless() ||
      pitch.octaveless() == Pitch(A, SHARP).octaveless()) SHARP else FLAT
    pitch.getNoteShift(3, accidental)
  } else pitch
  return sNotesToSharps[Pitch(actual.noteLetter, actual.accidental)]
}

fun getKeyPitch(sharps: Int): Pitch = sSharpsToNotes[sharps] ?: error("")

/* Transpose a key signature the specified amount */
fun transposeKey(oldSharps: Int, shift: Int, preferSharpOpt: Accidental? = null): Int {
  if (shift == 0) {
    /* no point doing any work */
    return oldSharps
  }

  return sSharpsToNotes[oldSharps]?.let { oldKeyLetter ->
    fun sharps(accidental: Accidental?) = getNewKeySharps(accidental, oldKeyLetter, shift)

    if (preferSharpOpt != null) {
      sharps(preferSharpOpt) ?: sharps(SHARP) ?: sharps(FLAT)
    } else {
      val numSharps = sharps(SHARP) ?: Int.MAX_VALUE
      val numFlats = sharps(FLAT) ?: Int.MAX_VALUE
      if (abs(numSharps) < abs(numFlats)) numSharps else numFlats
    }
  } ?: oldSharps
}

private fun getNewKeySharps(accidental: Accidental?, oldKeyLetter: Pitch, shift: Int): Int? {
  val note = getNoteShift(oldKeyLetter, shift, accidental ?: NATURAL)
  return if (note.octaveless() == Pitch(B, NATURAL).octaveless()) {
    if (accidental == SHARP) 5 else -7
  } else {
    getSharps(note)
  }
}

/* Get the distance in semitones between two key signatures */
fun keyDistance(fromSharps: Int, toSharps: Int, up: Boolean? = null): Int {
  return sSharpsToNotes[fromSharps]?.let { fromPitch ->
    sSharpsToNotes[toSharps]?.let { toPitch ->
      val one = (toPitch.midiVal - fromPitch.midiVal + 12) % 12
      val two = -((fromPitch.midiVal - toPitch.midiVal + 12) % 12)
      return up?.let {
        /* caller has specified direction */
        if (up) max(one, two) else min(one, two)
      } ?: run {
        /* Use the smaller of the two distances */
        if (abs(one) < abs(two)) one else two
      }
    }
  } ?: 0
}