package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*


import org.junit.Test

class PauseTest : ScoreTest() {

  @Test
  fun testAddPause() {
    SMV()
    SAE(EventType.PAUSE, ea(1), params = paramMapOf(EventParam.TYPE to PauseType.BREATH))
    SVP(EventType.PAUSE, EventParam.TYPE, PauseType.BREATH, ea(1))
  }

}