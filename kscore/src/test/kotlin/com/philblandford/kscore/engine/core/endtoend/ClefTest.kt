package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.minim
import core.representation.RepTest
import org.junit.Test

class ClefTest : RepTest() {

  @Test
  fun testAddClef() {
    SAE(EventType.CLEF, params = paramMapOf(EventParam.TYPE to ClefType.BASS), eventAddress = ea(2))
    RVA("Clef", ea(2))
  }

  @Test
  fun testAddClefMidBar() {
    SMV()
    SMV(eventAddress = eav(1, minim()))
    SAE(EventType.CLEF, params = paramMapOf(EventParam.TYPE to ClefType.BASS), eventAddress = ea(1, minim()))
    RVA("Clef", ea(1, minim()))
  }

  @Test
  fun testAddHeaderClefUnchanged() {
    SAE(EventType.CLEF, params = paramMapOf(EventParam.TYPE to ClefType.BASS), eventAddress = ea(2))
    assertEqual(ClefType.TREBLE, getArea("Clef", ea(1))?.area?.event?.subType)
  }
}