package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.types.*


import org.junit.Test

class SystemBreakTest : ScoreTest() {

  @Test
  fun testAddSystemBreak() {
    SMV()
    SAE(EventType.BREAK, ea(1), params = paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    SVP(EventType.BREAK, EventParam.TYPE, BreakType.SYSTEM, ez(1))
  }

  @Test
  fun testAddSystemBreakPart() {
    sc.setSelectedPart(1)
    SAE(EventType.BREAK, ez(1), params = paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    SVP(EventType.BREAK, EventParam.TYPE, BreakType.SYSTEM, ea(1))
  }

  @Test
  fun testTopLevelBreakIgnoredSelectedPart() {
    SAE(EventType.BREAK, ea(1), params = paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    sc.setSelectedPart(1)
    assertEqual(0, EG().getEvents(EventType.BREAK)?.size)
  }

  @Test
  fun testTopLevelBreakNotIgnoredSelectedPart() {
    sc.setSelectedPart(1)
    SAE(EventType.BREAK, ea(1), params = paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    assertEqual(1, EG().getEvents(EventType.BREAK)?.size)
  }

}