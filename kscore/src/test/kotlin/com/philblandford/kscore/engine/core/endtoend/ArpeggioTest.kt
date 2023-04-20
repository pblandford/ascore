package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eav
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class ArpeggioTest : RepTest() {

  @Test
  fun testArpeggioDrawn() {
    SMV()
    SMV(60)
    SAE(EventType.ARPEGGIO, eav(1))
    RVA("Arpeggio", eav(1))
  }

  @Test
  fun testArpeggioDrawnHeightCorrect() {
    SMV()
    SMV(60)
    SAE(EventType.ARPEGGIO, eav(1))
    val arp = getArea("Arpeggio", eav(1))!!
    assertEqual(10 * BLOCK_HEIGHT, arp.area.height)
  }
}