package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.engine.types.NoteLetter
import com.philblandford.kscore.engine.types.Pitch
import core.representation.RepTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


internal class StandaloneGeneratorTest : RepTest() {

  private val standaloneGenerator = StandaloneGenerator(TestDrawableGetter)

  @Test
  fun testCreateHeader() {
    val standalone = standaloneGenerator.getHeader(clef = ClefType.TREBLE, ks = 4)!!
    assert(standalone.width != 0)
    assert(standalone.height != 0)
  }

  @Test
  fun testCreateChord() {
    val standalone = standaloneGenerator.getChord(
      Chord(crotchet(), listOf(Note(crotchet(), Pitch(NoteLetter.A))))
    )!!
    assert(standalone.width != 0)
    assert(standalone.height != 0)
  }
}