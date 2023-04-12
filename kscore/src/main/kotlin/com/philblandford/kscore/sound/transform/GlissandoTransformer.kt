package com.philblandford.kscore.sound.transform

import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.duration.Offset


internal object GlissandoTransformer {

  fun transform(chord: Chord, ks: Int, nextChord: Chord): List<Pair<Offset, Chord>> {


    val start = chord.notes.first().pitch.midiVal
    val end = nextChord.notes.last().pitch.midiVal
    val range = if (start > end) (start downTo end) else (start..end)
    val adjustments = range.map { it - start }

    return OrnamentTransformer.ornamentTransform(chord, ks, adjustments, null, null)
  }

}
