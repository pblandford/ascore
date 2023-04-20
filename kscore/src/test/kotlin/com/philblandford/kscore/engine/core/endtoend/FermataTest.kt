package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.core.representation.RepTest
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.duration.*
import org.junit.Test

class FermataTest : RepTest() {

  @Test
  fun testFermata() {
    SAE(EventType.FERMATA, ez(1))
    RVA("Fermata", ea(1))
  }

  @Test
  fun testFermataTwoParts() {
    RCD(instruments = listOf("Violin", "Viola"))
    SAE(EventType.FERMATA, ez(1))
    RVA("Fermata", ea(1))
    RVA("Fermata", eas(1, dZero(), StaveId(2,1)))
  }

  @Test
  fun testFermataTwoPartsTwoPerBar() {
    RCD(instruments = listOf("Violin", "Viola"))
    SMV()
    SAE(EventType.FERMATA, ez(1))
    SAE(EventType.FERMATA, ez(1, minim()))
    RVA("Fermata", ea(1))
    RVA("Fermata", ea(1, minim()))
    RVA("Fermata", eas(1, dZero(), StaveId(2,1)))
    RVNA("Fermata", eas(1, minim(), StaveId(2,1)))
  }

  @Test
  fun testFermatasDontCollide() {
    SMV(duration = semiquaver())
    SMV(duration = semiquaver(), eventAddress = eav(1, semiquaver()))
    SAE(EventType.FERMATA, ez(1))
    SAE(EventType.FERMATA, ez(1, semiquaver()))
    assert(isLeft("Fermata", ea(1), "Fermata", ea(1, semiquaver()))!!)
  }

  @Test
  fun testFermataOverRepeatBar() {
    SAE(EventType.FERMATA, ez(2))
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    RVA("Fermata", ea(2))
  }

  @Test
  fun testFermataOverRepeatBarOnlyOne() {
    SMV()
    SAE(EventType.FERMATA, ez(2))
    SAE(EventType.FERMATA, ez(2, crotchet()))
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    assertEqual(1, getAreas("Fermata").size)
  }
}