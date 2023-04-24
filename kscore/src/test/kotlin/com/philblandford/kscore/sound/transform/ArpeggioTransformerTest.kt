package com.philblandford.kscore.sound.transform

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import org.junit.Test

class ArpeggioTransformerTest {

  @Test
  fun testArpeggio() {
    val chord = getChordA(crotchet())
    val transformed = ArpeggioTransformer.transform(chord)
    assertEqual(Array(3) { crotchet() - (ORNAMENT_MAX_NOTE * it) }.toList(),
      transformed.map { it.second.realDuration }.toList().take(3)
    )
  }
}

internal fun getChordA(duration: Duration): Chord {
  return Chord(
    duration,
    listOf(
      Note(duration, Pitch(NoteLetter.D)),
      Note(duration, Pitch(NoteLetter.F)),
      Note(duration, Pitch(NoteLetter.G)),
      Note(duration, Pitch(NoteLetter.A))
    ),
    arpeggio = ChordDecoration(true, listOf(ArpeggioType.NORMAL))
  )
}