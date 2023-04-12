package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.crotchet
import org.junit.Test

class GlissandoTest : ScoreTest() {

  @Test
  fun testAddGlissando() {
    SAE(EventType.GLISSANDO, ea(1), paramMapOf(EventParam.IS_STRAIGHT to false))
    SVP(EventType.GLISSANDO, EventParam.IS_STRAIGHT, false, ea(1))
  }

  @Test
  fun testAddGlissandoEndCreated() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.GLISSANDO, ea(1), paramMapOf(EventParam.IS_STRAIGHT to false,
      EventParam.IS_UP to false))
    SVP(EventType.GLISSANDO, EventParam.END, true, ea(1, crotchet()))
  }

}