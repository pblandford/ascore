package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*

import org.junit.Test

class DynamicTest : ScoreTest() {

  @Test
  fun testAddDynamic() {
    SAE(EventType.DYNAMIC, ea(1), paramMapOf(EventParam.TYPE to DynamicType.FORTE,
      EventParam.IS_UP to true))
    SVE(EventType.DYNAMIC, ea(1))
  }

  @Test
  fun testAddDynamicVoiceIgnored() {
    SAE(EventType.DYNAMIC, eav(1), paramMapOf(EventParam.TYPE to DynamicType.FORTE,
      EventParam.IS_UP to true))
    SVE(EventType.DYNAMIC, ea(1))
  }

}