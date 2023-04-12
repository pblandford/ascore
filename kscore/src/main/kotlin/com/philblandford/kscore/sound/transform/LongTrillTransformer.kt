package com.philblandford.kscore.sound.transform

import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.div
import com.philblandford.kscore.engine.types.Accidental


object LongTrillTransformer {

  fun transform(chord: Chord, ks:Int, accidentalAbove:Accidental? = null): List<Pair<Offset, Chord>> {

    val numNotes = (chord.realDuration / ORNAMENT_MAX_NOTE).toInt()

    val adjustments = (0 until numNotes).map { it % 2 }
    return OrnamentTransformer.ornamentTransform(chord, ks, adjustments, accidentalAbove, null)
  }

}
