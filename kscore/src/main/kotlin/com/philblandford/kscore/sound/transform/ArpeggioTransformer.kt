package com.philblandford.kscore.sound.transform

import com.philblandford.kscore.engine.duration.*

private val MAX_NOTE_LENGTH = demisemiquaver()


object ArpeggioTransformer {
  fun transform(chord: Chord): List<Pair<Offset, Chord>> {

    val num = chord.notes.count() - 1
    if (num <= 0) {
      return listOf(dZero() to chord)
    }
    val noteLength = minOf(chord.realDuration / num, MAX_NOTE_LENGTH)
    val arpNotes = chord.notes.drop(1).sortedBy { it.pitch.midiVal }.withIndex().map { iv ->
      val start = noteLength * iv.index
      start to iv.value.copy(
        duration = chord.duration - start,
        realDuration = chord.duration - start
      )
    }
    val soFar = noteLength*num
    val remainder = chord.realDuration - soFar
    val notes = if (remainder > dZero()) {
      val last = chord.notes.first().copy(duration = remainder, realDuration = remainder)
      arpNotes.plus(soFar to last)
    } else {
      arpNotes
    }
    return notes.map {
      it.first to Chord(
        it.second.duration,
        listOf(it.second),
        realDuration = it.second.realDuration
      )
    }
  }

}
