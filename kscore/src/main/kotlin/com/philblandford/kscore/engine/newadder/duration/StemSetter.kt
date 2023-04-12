package com.philblandford.kscore.engine.newadder.duration

import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.newadder.util.getStemDirection
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.Modifiable

fun Event.setStem(numVoices: Int, voice: Int): Event {
  return chord(this)?.let { chord ->
    if (chord.isUpstem.modified) {
      chord
    } else {
      val up = if (numVoices > 1) {
        voice % 2 == 1
      } else {
        getStemDirection(chord.notes.map { it.position }, if (numVoices > 1) voice else null)
      }
      chord.copy(isUpstem = Modifiable(false, up))
    }
  }?.toEvent() ?: this
}
