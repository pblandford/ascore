package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class StaveTest : ScoreTest() {

  @Test
  fun testAddStave() {
    SAE(EventType.STAVE, ea(1), paramMapOf(EventParam.CLEF to listOf(ClefType.TREBLE, ClefType.BASS)))
    assert(EG().getStave(StaveId(1,2)) != null)
  }

  @Test
  fun testDeleteStave() {
    SCD(instruments = listOf("Piano"))
    SAE(EventType.STAVE, ea(1), paramMapOf(EventParam.CLEF to listOf(ClefType.TREBLE)))
    assert(EG().getStave(StaveId(1,1)) != null)
    assert(EG().getStave(StaveId(1,2)) == null)
  }

  @Test
  fun testAddStavePartRemainsInPlace() {
    SCD(instruments = listOf("Violin", "Viola"))
    SAE(EventType.STAVE, ea(1), paramMapOf(EventParam.CLEF to listOf(ClefType.TREBLE, ClefType.BASS)))
    assert(EG().getStave(StaveId(1,2)) != null)
    assert(EG().getStave(StaveId(2,2)) == null)
  }

  @Test
  fun testAddStavePartLabelRetained() {
    SAE(EventType.STAVE, ea(1), paramMapOf(EventParam.CLEF to listOf(ClefType.TREBLE, ClefType.BASS)))
    assertEqual(EG().getPart(1)?.label, "Violin")
  }

  @Test
  fun testDeleteStaveLabelRetained() {
    SCD(instruments = listOf("Piano"))
    SAE(EventType.STAVE, ea(1), paramMapOf(EventParam.CLEF to listOf(ClefType.TREBLE)))
    assertEqual(EG().getPart(1)?.label, "Piano")
  }

}