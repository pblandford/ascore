package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class TempoTextTest : RepTest() {

  @Test
  fun testAddTempoText() {
    SAE(EventType.TEMPO_TEXT, ea(1), paramMapOf(EventParam.TEXT to "Wibble"))
    RVA("TempoText", ez(1))
  }

}