package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*


import org.junit.Test

class SpaceTest : ScoreTest() {

  @Test
  fun testAddSpace() {
    SMV()
    SAE(EventType.SPACE, ea(1), params = paramMapOf(EventParam.AMOUNT to 20))
    SVP(EventType.SPACE, EventParam.AMOUNT, 20, ea(1))
  }

}