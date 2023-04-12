package com.philblandford.kscore.sound.transform

import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.div
import com.philblandford.kscore.engine.duration.times


object TremoloTransformer {
  fun transform(chord: Chord): Map<Offset, Chord>? {

    return chord.tremoloBeats?.let { cd ->
      val beats = cd.items.first()
      val num = (chord.realDuration / beats).toInt()

      if (num > 0) {
        val noteLength = chord.realDuration / num
        (0 until num).map { n ->
          var newChord = chord.setDuration(noteLength)
          newChord = newChord.transformNotes { it.copy(isStartTie = false, isEndTie = false) }
          noteLength * n to newChord
        }
      } else null

    }?.toMap()
  }

}
