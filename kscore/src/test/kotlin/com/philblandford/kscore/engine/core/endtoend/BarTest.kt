package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*

import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class BarTest : RepTest() {

  @Test
  fun testAddBar() {
    val numBars = EG().numBars
    SAE(EventType.BAR, ez(1), params = paramMapOf(EventParam.NUMBER to 1))
    val rests = getAreas("Rest")
    assertEqual(numBars+1, rests.size)
  }

  @Test
  fun testAddBarBefore() {
    SMV()
    SAE(EventType.BAR, ez(1), params = paramMapOf(EventParam.NUMBER to 1))
    RVA("Rest", eav(1))
    RVA("Chord", eav(2))
  }

  @Test
  fun testCreateScoreZeroBars() {
    RCD(bars = 0)
    assert(sc.currentScoreState.value.representation == null)
  }
}
