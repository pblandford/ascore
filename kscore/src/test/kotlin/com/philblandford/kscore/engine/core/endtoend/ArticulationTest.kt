package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.quaver
import core.representation.RepTest
import org.junit.Test

class ArticulationTest : RepTest() {

  @Test
  fun testAddArticulation() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.STACCATO))
    RVA("Staccato", eav(1))
  }

  @Test
  fun testAddTwoArticulations() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.STACCATO))
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    RVA("Staccato", eav(1))
    RVA("Accent", eav(1))
  }

  @Test
  fun testAddTwoArticulationsAccentAboveStaccato() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.STACCATO))
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    assert(isAbove("Accent", eav(1), "Staccato", eav(1))!!)
  }

  @Test
  fun testAddArticulationTwoVoices() {
    SMV()
    SMV(eventAddress = eav(1, dZero(), 2))
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.STACCATO))
    assert(isAbove("Staccato", eav(1), "Stem", eav(1))!!)
  }

  @Test
  fun testArticulationBeamed() {
    SMV(71, quaver())
    SMV(69, quaver(), eventAddress = eav(1, quaver()))
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.STACCATO))
    assert(isAbove("Tadpole", eav(1).copy(id = 1), "Staccato", eav(1))!!)
  }
}