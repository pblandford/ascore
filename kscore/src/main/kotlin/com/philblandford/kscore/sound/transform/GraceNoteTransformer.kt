package com.philblandford.kscore.sound.transform

import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.types.Event

internal val GRACE_NOTE_SEMIQUAVER_DURATION = demisemiquaver()
private val ratio = GRACE_NOTE_SEMIQUAVER_DURATION / semiquaver()

object GraceNoteTransformer {
  fun transform(chord: Event, graceNotes: Map<Offset, Event>): Map<Offset, Event>? {

    var offset = dZero()
    val grace = graceNotes.mapNotNull {

      val duration = it.value.realDuration() * ratio
      val o = offset
      offset += duration
      chord(it.value)?.setDuration(duration)?.let { o to it.toEvent() }
    }
    val total = grace.map { it.second.realDuration() }.reduce { a, b -> a + b }
    val chordDuration = chord.realDuration() - total
    val setChord = chord(chord)?.setDuration(chordDuration)?.toEvent() ?: chord.setDuration(chordDuration)

    return grace.plus(offset to setChord).toMap()
  }



}
