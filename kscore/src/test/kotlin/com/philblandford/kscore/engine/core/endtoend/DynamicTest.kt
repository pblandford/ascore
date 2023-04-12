package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.*
import core.representation.RepTest
import org.junit.Test

class DynamicTest : RepTest() {

  @Test
  fun testAddDynamic() {
    SAE(EventType.DYNAMIC, params = paramMapOf(EventParam.TYPE to DynamicType.FORTE, EventParam.IS_UP to false))
    RVA("Dynamic", ea(1).copy(id = 1))
  }

  @Test
  fun testAddDynamicUnderStave() {
    SAE(EventType.DYNAMIC, params = paramMapOf(EventParam.TYPE to DynamicType.FORTE, EventParam.IS_UP to false))
    assert(isAbove("StaveLines", ea(1), "Dynamic", (ea(1).copy(id = 1)))!!)
  }

  @Test
  fun testAddDynamicUnderNote() {
    SMV(midiVal = 55)
    SAE(EventType.DYNAMIC, params = paramMapOf(EventParam.TYPE to DynamicType.FORTE, EventParam.IS_UP to false))
    assert(isAbove("Chord", eav(1), "Dynamic", (ea(1).copy(id = 1)))!!)
  }
}