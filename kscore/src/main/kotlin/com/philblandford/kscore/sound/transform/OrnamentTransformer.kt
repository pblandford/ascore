package com.philblandford.kscore.sound.transform

import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.pitch.pitchAtStep
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.OrnamentType

internal val ORNAMENT_MAX_NOTE = demisemiquaver()

object OrnamentTransformer {
  fun transform(chord: Chord, ks: Int): List<Pair<Offset, Chord>> {

    return chord.ornament?.let { chordDec ->
      val ornament = chordDec.items.first()
      val adjustments = when (ornament.ornamentType) {
        OrnamentType.TRILL -> listOf(0, 1, 0, 1)
        OrnamentType.TURN -> listOf(0, 1, 0, -1)
        OrnamentType.MORDENT -> listOf(0, 1)
        OrnamentType.LOWER_MORDENT -> listOf(0, -1)
      }
      ornamentTransform(
        chord, ks, adjustments, ornament.accidentalAbove,
        ornament.accidentalBelow
      )

    } ?: mapOf(dZero() to chord).toList()
  }

  internal fun ornamentTransform(
    chord: Chord, ks: Int, adjustments: Iterable<Int>,
    accidentalAbove: Accidental?, accidentalBelow: Accidental?
  ): List<Pair<Offset, Chord>> {
    val top = transformOrnament(
      chord.notes.toList().first(), chord.realDuration, adjustments, ks,
      accidentalAbove, accidentalBelow
    )
    val rest = chord.removeNote(0)
    return if (rest.notes.count() > 0) {
      top.plus(dZero() to rest)
    } else {
      top
    }
  }

  private fun transformOrnament(
    note: Note, duration: Duration,
    adjustments: Iterable<Int>,
    ks: Int, accidentalAbove: Accidental?, accidentalBelow: Accidental?
  ): List<Pair<Offset, Chord>> {
    val num = adjustments.count()
    val noteLength = minOf(duration / num, ORNAMENT_MAX_NOTE)

    val turnNotes = adjustments.withIndex().map { iv ->
      val accidental =
        if (iv.value > 0) accidentalAbove else if (iv.value < 0) accidentalBelow else null
      val newPitch = note.pitch.pitchAtStep(iv.value, ks, true, accidental)
      val offset = noteLength * iv.index
      offset to note.copy(pitch = newPitch, duration = noteLength, realDuration = noteLength)
    }
    val remainder = duration - noteLength * num
    val notes = if (remainder > dZero()) {
      val offset = noteLength * num
      val last = note.copy(realDuration = remainder)
      turnNotes.plus(offset to last)
    } else {
      turnNotes
    }
    return notes.map { (offset, note) ->
      offset to Chord(note.duration, listOf(note), realDuration = note.realDuration)
    }
  }

}
