package com.philblandford.kscore.sound.transform

import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.times
import com.philblandford.kscore.engine.types.ArticulationType
import org.apache.commons.math3.fraction.Fraction
import kotlin.math.min

private val MAX_NOTE_LENGTH = 50

internal val STACCATO_LENGTH = Fraction(3, 4)
internal val STACCATISSIMO_LENGTH = Fraction(1, 2)

object ArticulationTransformer {
  fun transform(
    chord: Chord
  ): Chord {

    val articulations = chord.articulations?.items ?: listOf()

    val newDuration = articulations.fold(chord.realDuration) { d, a -> adjustDuration(d, a) }

    val newChord = chord.transformNotes { cn ->
      cn.copy(realDuration = newDuration)
    }
    return newChord.copy(realDuration = newDuration)
  }


  private fun adjustDuration(duration: Duration, articulationType: ArticulationType): Duration {
    return when (articulationType) {
      ArticulationType.STACCATO -> (duration * STACCATO_LENGTH)
      ArticulationType.STACCATISSIMO -> (duration * STACCATISSIMO_LENGTH)
      else -> duration
    }
  }

  private fun adjustVelocity(velocity: Int, articulationType: ArticulationType): Int {
    val newV = when (articulationType) {
      ArticulationType.ACCENT -> (velocity * 1.25).toInt()
      ArticulationType.MARCATO -> (velocity * 1.4).toInt()
      else -> velocity
    }
    return min(newV, 127)
  }
}
