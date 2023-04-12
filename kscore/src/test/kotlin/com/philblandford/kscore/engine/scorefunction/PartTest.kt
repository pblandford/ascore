package com.philblandford.kscore.engine.scorefunction

import assertEqual

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.ez
import org.junit.Test

class PartTest : ScoreTest() {

  @Test
  fun testGetPartLabel() {
    val label = EG().getPart(1)?.label
    assertEqual("Violin", label)
  }

  @Test
  fun testSetPartLabel() {
    SSP(EventType.PART, EventParam.LABEL, "Wibble", ea(1))
    SVP(EventType.PART, EventParam.LABEL, "Wibble", ea(1))
  }

  @Test
  fun testSetPartLabelDoesntIncreaseNumberOfParts() {
    SSP(EventType.PART, EventParam.LABEL, "Wibble", ea(1))
    assertEqual(1, EG().numParts)
  }

  @Test
  fun testSetPartLabelFont() {
    SSP(EventType.PART, EventParam.FONT, "Wibble", ea(1))
    SVP(EventType.PART, EventParam.FONT, "Wibble", ea(1))
  }

  @Test
  fun testSetPartLabelTextSize() {
    SSP(EventType.PART, EventParam.TEXT_SIZE, 200, ea(1))
    SVP(EventType.PART, EventParam.TEXT_SIZE, 200, ea(1))
  }

  @Test
  fun testSetPartLabelChangeTimeSignature() {
    SSP(EventType.PART, EventParam.LABEL, "Wibble", ea(1))
    SAE(TimeSignature(3,4).toEvent(), ez(1))
    SVP(EventType.PART, EventParam.LABEL, "Wibble", ea(1))
  }

}