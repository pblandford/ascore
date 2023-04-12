package com.philblandford.kscore.sound.transform

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.*
import org.junit.Test

class LongTrillTransformerTest {

  @Test
  fun testTransformLongTrill() {
    val chord = chord(dslChord (crotchet()))!!
    val transformed = LongTrillTransformer.transform(chord, 0)
    assertEqual(
      Array(4) { ORNAMENT_MAX_NOTE }.toList(),
      transformed.map { it.second.realDuration }.take(4).toList()
    )
  }

  @Test
  fun testTransformLongTrillNoteAbove() {
    val chord = chord(dslChord (crotchet()))!!
    val transformed = LongTrillTransformer.transform(chord, 0)
    assertEqual(Pitch(NoteLetter.F, Accidental.NATURAL, 4), transformed.toList()[0].second.notes.first().pitch)
    assertEqual(Pitch(NoteLetter.G, Accidental.NATURAL, 4), transformed.toList()[1].second.notes.first().pitch)
  }

  @Test
  fun testTransformLongTrillNoteAboveKeySignature() {
    val chord = chord(dslChord (crotchet()))!!
    val transformed = LongTrillTransformer.transform(chord, 1)
    assertEqual(Pitch(NoteLetter.F, Accidental.SHARP, 4), transformed.toList()[0].second.notes.first().pitch)
    assertEqual(Pitch(NoteLetter.G, Accidental.NATURAL, 4), transformed.toList()[1].second.notes.first().pitch)
  }

  @Test
  fun testTransformLongTrillAccidentalAbove() {
    val chord = chord(dslChord (crotchet()))!!
    val transformed = LongTrillTransformer.transform(chord, 0, Accidental.FLAT)
    assertEqual(Pitch(NoteLetter.F, Accidental.NATURAL, 4), transformed.toList()[0].second.notes.first().pitch)
    assertEqual(Pitch(NoteLetter.G, Accidental.FLAT, 4), transformed.toList()[1].second.notes.first().pitch)
  }

}

