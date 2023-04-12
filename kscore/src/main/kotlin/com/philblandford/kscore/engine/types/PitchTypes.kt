package com.philblandford.kscore.engine.types

import com.philblandford.kscore.engine.pitch.getKeyPitch
import com.philblandford.kscore.engine.pitch.getMidiVal
import com.philblandford.kscore.engine.pitch.getPitchFromMidiVal
import com.philblandford.kscore.engine.pitch.pitchesInScale

enum class NoteLetter {
  C, D, E, F, G, A, B;

  fun increment(step: Int): NoteLetter {
    return values()[(this.ordinal + 7*10 + step) % 7]
  }

  fun diff(other: NoteLetter): Int {
    return (this.ordinal - other.ordinal + 7) % 7
  }
}

enum class Accidental {
  DOUBLE_FLAT, FLAT, NATURAL, SHARP, DOUBLE_SHARP, FORCE_SHARP, FORCE_FLAT;

  fun toChar(showNatural: Boolean = false): String {
    return when (this) {
      SHARP -> "#"//"♯"
      FLAT -> "b"//"♭"
      DOUBLE_SHARP -> "\uD834\uDD2A"
      DOUBLE_FLAT -> "bb"//"♭♭"
      else -> if (showNatural) {
        "♮"
      } else {
        ""
      }
    }
  }

}

// octave is sounding pitch, not written
data class Pitch(
  val noteLetter: NoteLetter, val accidental: Accidental = Accidental.NATURAL, val octave: Int = 4,
  val showAccidental: Boolean = false
) {
  val midiVal = getMidiVal() ?: 0

  companion object {
    val allPitches = NoteLetter.values().flatMap { note ->
      listOf(Accidental.FLAT, Accidental.NATURAL, Accidental.SHARP).map { acc ->
        Pitch(note, acc, 0)
      }
    }
    val allStrings = allPitches.map { it.letterString() to it }.toMap()
  }

  fun letterString(): String {
    return "$noteLetter${accidental.toChar()}"
  }

  fun shiftOctave(num:Int):Pitch {
    return Pitch(noteLetter, accidental, octave + num)
  }

  fun octaveless() = Pitch(noteLetter, accidental, 0, false)

}

fun unPitched() = Pitch(NoteLetter.A, octave = 0)

private val symbols = mapOf(
  Accidental.FLAT to "b",// "\u266d",
  Accidental.NATURAL to "\u266e",
  Accidental.SHARP to "#",//"\u266f",
  Accidental.DOUBLE_FLAT to "\u1d12b",
  Accidental.DOUBLE_SHARP to "\u1d12a"
)

fun getSymbol(accidental: Accidental): String {
  return when (accidental) {
    Accidental.SHARP, Accidental.FLAT -> symbols[accidental] ?: ""
    Accidental.NATURAL -> ""
    else -> ""
  }
}


fun getNoteShift(
  pitch: Pitch,
  shift: Int,
  preferredEventSubType: Accidental = Accidental.NATURAL
): Pitch {
  val newMidiVal = pitch.midiVal + shift
  return getPitchFromMidiVal(newMidiVal, preferredEventSubType)
}

private val majorScale = listOf(2, 3, 5, 7, 8, 10)
fun getScaleDegree(key: Int, degree: Int): Pitch {
  val home = getKeyPitch(key)
  return home.pitchesInScale(key).toList()[degree]
}

operator fun Pitch.plus(shift:Int):Pitch {
  return getNoteShift(this, shift)
}

operator fun Pitch.minus(shift:Int):Pitch {
  return getNoteShift(this, -shift)
}

