package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class FingeringTest : RepTest() {

  @Test
  fun testAddFingering() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 1))
    RVA("Fingering", eav(1))
  }

  @Test
  fun testAddFingeringAbove() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.IS_UP to true, EventParam.NUMBER to 1))
    assertEqual(true, isAbove("Fingering", eav(1), "Tadpole", eav(1).copy(id = 1)))
  }

  @Test
  fun testAddFingeringBelow() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.IS_UP to false, EventParam.NUMBER to 1))
    assertEqual(false, isAbove("Fingering", eav(1), "Tadpole", eav(1).copy(id = 1)))
  }

  @Test
  fun testAddFingeringAboveUpstem() {
    SMV(69)
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.IS_UP to true, EventParam.NUMBER to 1))
    assert(isAbove("Fingering", eav(1), "Tadpole", eav(1).copy(id = 1)) == true)
  }
}