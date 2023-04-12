package com.philblandford.kscore.engine.scorefunction


import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.breve
import com.philblandford.kscore.engine.duration.semibreve
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eav
import org.junit.Test

class ChordTest : ScoreTest() {

  @Test
  fun testAddChord() {
    val chord = dslChord()
    SAE(chord, eav(1))
    SVVM("C4:R4:R2", eav(1))
  }

  @Test
  fun testAddChordAcrossBar() {
    val chord = dslChord(breve())
    SAE(chord, eav(1))
    SVVM("C1", eav(1))
    SVVM("C1", eav(2))
  }

  @Test
  fun testAddChordAcrossBarNotesChanged() {
    val chord = dslChord(breve())
    SAE(chord, eav(1))
    SVP(EventType.NOTE, EventParam.DURATION, semibreve(), eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteChord() {
    val chord = dslChord()
    SAE(chord, eav(1))
    SDE(EventType.DURATION, eav(1))
    SVVM("", eav(1))
  }
}