package com.philblandford.kscore.engine.pitch

import assertEqual
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.types.NoteLetter.*
import com.philblandford.kscore.engine.types.Accidental.*
import com.philblandford.kscore.engine.types.NoteLetter
import com.philblandford.kscore.engine.types.Pitch
import junit.framework.Assert.assertEquals
import org.junit.Test

class TransposerTest {

  @Test
  fun testTransposePitch() {
    val res = Transposer.transposePitch(Pitch(C, NATURAL, 4), 0, 2)
    assertEqual(Pitch(D, NATURAL, 4), res)
  }

  @Test
  fun testTransposeHarmony() {
    val res = Transposer.transposeHarmony(Harmony(Pitch(C, NATURAL, 4), ""), 0, 2)
    assertEqual(Pitch(D, NATURAL, 4), res.tone)

  }

  @Test
  fun testTransposeChord() {
    val chord = chord(dslChord { pitch(C, NATURAL, 5) })!!
    val transposedChord = Transposer.transposeChord(chord, 0, 2)
    assertEquals(Pitch(NoteLetter.D, NATURAL, 5), transposedChord.notes.first().pitch)
  }

  @Test
  fun testTransposePitchFSharp3Sharps() {
    val pitch = Pitch(G, SHARP, 5)
    assertEquals(Pitch(F, SHARP, 5), Transposer.transposePitch(pitch, 3, -2))
  }

  @Test
  fun testTranspose0StepsSharp() {
    val pitch = Pitch(G, SHARP, 5)
    assertEquals(Pitch(G, SHARP, 5), Transposer.transposePitch(pitch, 3, 0))
  }

  @Test
  fun testTransposeToBSharp() {
    val pitch = Pitch(G, SHARP, 5)
    assertEquals(Pitch(B, SHARP, 6), Transposer.transposePitch(pitch, 3, 4, SHARP))
  }

  @Test
  fun testTransposeBSharpToASharp() {
    val pitch = Pitch(B, SHARP, 5)
    assertEquals(Pitch(A, SHARP, 4), Transposer.transposePitch(pitch, 1, -2, SHARP))
  }

  @Test
  fun testTransposeCtoCSharp() {
    val pitch = Pitch(C, NATURAL, 4)
    assertEquals(Pitch(C, SHARP, 4), Transposer.transposePitch(pitch, 0, 1, SHARP))
  }

  @Test
  fun testTransposeCFlatToDb() {
    val pitch = Pitch(C, FLAT, 4)
    assertEquals(Pitch(D, FLAT, 5), Transposer.transposePitch(pitch, 1, 2, FLAT))
  }

  @Test
  fun testTransposeCFlatDown1() {
    val pitch = Pitch(C, FLAT, 4)
    assertEquals(Pitch(B, FLAT, 4), Transposer.transposePitch(pitch, 0, -1, FLAT))
  }


  @Test
  fun testTransposeASharpFromCtoEb() {
    val pitch = Pitch(A, SHARP, 5)
    assertEquals(Pitch(C, SHARP, 6), Transposer.transposePitch(pitch, 0, 3))
  }

  @Test
  fun testTransposeDDoubleFlatDown2() {
    val pitch = Pitch(D, DOUBLE_FLAT, 5)
    assertEquals(Pitch(B, FLAT, 4), Transposer.transposePitch(pitch, 5, -2))
  }

  @Test
  fun testTransposeCSharpDown1() {
    val pitch = Pitch(C, SHARP, 4)
    assertEquals(Pitch(B, SHARP, 4), Transposer.transposePitch(pitch, 0, -1, SHARP))
  }

  @Test
  fun testTransposeBDoubleFlatUp2() {
    val pitch = Pitch(B, DOUBLE_FLAT, 5)
    assertEquals(Pitch(C, FLAT, 5), Transposer.transposePitch(pitch, 5, 2, SHARP))
  }

  @Test
  fun testTransposeBFlatUp1() {
    val pitch = Pitch(B, FLAT, 4)
    assertEquals(Pitch(B, NATURAL, 4), Transposer.transposePitch(pitch, 0, 1, SHARP))
  }

  @Test
  fun testTransposeBFlatUp1FlatKey() {
    val pitch = Pitch(B, FLAT, 4)
    assertEquals(Pitch(C, FLAT, 4), Transposer.transposePitch(pitch, 0, 1, FLAT))
  }

  @Test
  fun testTransposeBSharpUp1FlatKey() {
    val pitch = Pitch(B, SHARP, 4)
    assertEquals(Pitch(C, SHARP, 4), Transposer.transposePitch(pitch, 0, 1, FLAT))
  }

  @Test
  fun testTransposeBDoubleFlatUp2ToFlatKey() {
    val pitch = Pitch(B, DOUBLE_FLAT, 5)
    assertEquals(Pitch(B, NATURAL, 5), Transposer.transposePitch(pitch, 4, 2, FLAT))
  }

  @Test
  fun testTransposeGbInEmajor() {
    val pitch = Pitch(NoteLetter.G, FLAT, 5)
    assertEquals(Pitch(A, FLAT, 5), Transposer.transposePitch(pitch, 4, 2))
  }

}