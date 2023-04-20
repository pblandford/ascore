package com.philblandford.kscore.engine.core.endtoend


import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.eas

import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class InstrumentTest : RepTest() {

  @Test
  fun testAddInstrument() {
    val instrument = instrumentGetter.getInstrument("Viola")!!
    SAE(instrument.toEvent().copy(EventType.PART), ea(1))

    RVA("Part", eas(1, 1, 0))
    RVA("Part", eas(1, 2, 0))
  }
}