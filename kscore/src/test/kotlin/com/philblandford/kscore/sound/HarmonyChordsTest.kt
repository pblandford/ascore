package com.philblandford.kscore.sound

import assertEqual
import com.philblandford.kscore.engine.types.NoteLetter.*
import com.philblandford.kscore.engine.pitch.harmony
import org.junit.Test

class HarmonyChordsTest {

  @Test
  fun testCreateCMajor() {
    val harmony = harmony("C")!!
    val chord = createHarmonyChord(harmony)
    assertEqual(listOf(C, E, G).toList(), chord!!.notes.map{ it.pitch.noteLetter}.toList())
  }

  @Test
  fun testCreateRootNote() {
    val harmony = harmony("C/E")!!
    val chord = createHarmonyChord(harmony)
    assertEqual(listOf(E, C, E, G).toList(), chord!!.notes.map{ it.pitch.noteLetter}.toList())
  }

}