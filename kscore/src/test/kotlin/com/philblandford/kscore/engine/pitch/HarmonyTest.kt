package com.philblandford.kscore.engine.pitch

import assertEqual
import com.philblandford.kscore.engine.types.Pitch
import com.philblandford.kscore.engine.types.NoteLetter.*
import com.philblandford.kscore.engine.types.Accidental.*
import org.junit.Test

class HarmonyTest {

  @Test
  fun testCreateFromString() {
    val harmony = harmony("C")
    assertEqual(Harmony(Pitch(C, NATURAL, 0), ""), harmony)
  }

  @Test
  fun testCreateFromStringAccidental() {
    val harmony = harmony("C#")
    assertEqual(Harmony(Pitch(C, SHARP, 0), ""), harmony)
  }

  @Test
  fun testCreateFromStringMinor() {
    val harmony = harmony("C#m")
    assertEqual(Harmony(Pitch(C, SHARP, 0), "m"), harmony)
  }

  @Test
  fun testCreateFromStringRoot() {
    val harmony = harmony("C#m/G")
    assertEqual(Harmony(Pitch(C, SHARP, 0), "m", Pitch(G, NATURAL, 0)), harmony)
  }

  @Test
  fun testToString() {
    val harmony = harmony("C")
    assertEqual("C", harmony.toString())
  }

  @Test
  fun testToStringAccidental() {
    val harmony = harmony("C#")
    assertEqual("C#", harmony.toString())
  }

  @Test
  fun testToStringRoot() {
    val harmony = harmony("C/G")
    assertEqual("C/G", harmony.toString())
  }

  @Test
  fun testToStringQuality() {
    val harmony = harmony("Cm7")
    assertEqual("Cm7", harmony.toString())
  }

  @Test
  fun testGetPitches() {
    val harmony = harmony("C")!!
    assertEqual(listOf(C,E,G).toList(), harmony.pitches().map { it.noteLetter }.toList())
    assertEqual(listOf(NATURAL,NATURAL,NATURAL).toList(), harmony.pitches().map { it.accidental }.toList())
  }

  @Test
  fun testGetPitchesMinor() {
    val harmony = harmony("Cm")!!
    assertEqual(listOf(60,63,67).toList(), harmony.pitches(4).map { it.midiVal }.toList())
  }

  @Test
  fun testGetPitchesOctave() {
    val harmony = harmony("C")!!
    assertEqual(listOf(2,2,2).toList(), harmony.pitches(2).map { it.octave }.toList())
  }

  @Test
  fun testGetPitchesAccidentalsCorrect() {
    val harmony = harmony("Eb")!!
    assertEqual(listOf(E,G,B).toList(), harmony.pitches().map { it.noteLetter }.toList())
    assertEqual(listOf(FLAT,NATURAL,FLAT).toList(), harmony.pitches().map { it.accidental }.toList())
  }

  @Test
  fun testGetPitchesAccidentalsCorrectMinor() {
    val harmony = harmony("Gm")!!
    assertEqual(listOf(G,B,D).toList(), harmony.pitches().map { it.noteLetter }.toList())
    assertEqual(listOf(NATURAL,FLAT,NATURAL).toList(), harmony.pitches().map { it.accidental }.toList())
  }

  @Test
  fun testGetPitchesAccidentalsCorrectMinorSharps() {
    val harmony = harmony("D#m")!!
    assertEqual(listOf(D,F,A).toList(), harmony.pitches().map { it.noteLetter }.toList())
    assertEqual(listOf(SHARP,SHARP,SHARP).toList(), harmony.pitches().map { it.accidental }.toList())
  }

  @Test
  fun testGetPitchesAccidentalsCorrectMinorDFlat() {
    val harmony = harmony("Dbm")!!
    assertEqual(listOf(D,F,A).toList(), harmony.pitches().map { it.noteLetter }.toList())
    assertEqual(listOf(FLAT,FLAT,FLAT).toList(), harmony.pitches().map { it.accidental }.toList())
  }

  @Test
  fun testGetPitchesAccidentalsCorrectMinorGFlat() {
    val harmony = harmony("Gbm")!!
    assertEqual(listOf(G,B,D).toList(), harmony.pitches().map { it.noteLetter }.toList())
    assertEqual(listOf(FLAT,DOUBLE_FLAT,FLAT).toList(), harmony.pitches().map { it.accidental }.toList())
  }

  @Test
  fun testGetPitchesTriad() {
    val harmony = harmony("C", 4)!!
    val pitches = harmony.pitches()
    assertEqual(listOf(C,E,G).toList(), pitches.map { it.noteLetter }.toList() )
    assertEqual(listOf(4,4,4).toList(), pitches.map { it.octave }.toList())
  }

  @Test
  fun testGetPitchesTriadAcrossBreak() {
    val harmony = harmony("A", 4)!!
    val pitches = harmony.pitches()
    assertEqual(listOf(A,C,E).toList(), pitches.map { it.noteLetter }.toList() )
    assertEqual(listOf(4,5,5).toList(), pitches.map { it.octave }.toList())
  }

  @Test
  fun testGetPitchesTriadFirstInversion() {
    val harmony = harmony("C", 4)!!
    val pitches = harmony.pitches(startOffset = 1)
    assertEqual(listOf(E,G,C).toList(), pitches.map { it.noteLetter }.toList() )
    assertEqual(listOf(4,4,5).toList(), pitches.map { it.octave }.toList())
  }

  @Test
  fun testGetPitchesTriadSecondInversion() {
    val harmony = harmony("C", 4)!!
    val pitches = harmony.pitches(startOffset = 2)
    assertEqual(listOf(G,C,E).toList(), pitches.map { it.noteLetter }.toList() )
    assertEqual(listOf(4,5,5).toList(), pitches.map { it.octave }.toList())
  }
}