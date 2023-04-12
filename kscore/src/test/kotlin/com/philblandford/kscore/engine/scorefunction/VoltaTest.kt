package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.types.*

import org.junit.Test

class VoltaTest : ScoreTest() {

  @Test
  fun testAddVolta() {
    SAE(EventType.VOLTA, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to 1))
    SVP(EventType.VOLTA, EventParam.NUM_BARS, 2, ez(1))
  }

  @Test
  fun testAddVoltaEndCreated()  {
    SAE(EventType.VOLTA, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to 1))
    SVP(EventType.VOLTA, EventParam.NUM_BARS, 2, ez(2))
    SVP(EventType.VOLTA, EventParam.END, true, ez(2))
  }

  @Test
  fun testAddVoltaDontOverlap() {
    SAE(EventType.VOLTA, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to 1))
    SAE(EventType.VOLTA, ea(2), paramMapOf(EventParam.END to ea(3), EventParam.NUMBER to 2))
    SVNE(EventType.VOLTA, ez(1))
  }

  @Test
  fun testAddVoltaConsecutive() {
    SAE(EventType.VOLTA, ea(1), paramMapOf(EventParam.END to ea(1), EventParam.NUMBER to 1))
    SAE(EventType.VOLTA, ea(2), paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to 2))
    SVE(EventType.VOLTA, ez(1))
    SVE(EventType.VOLTA, ez(2))
  }

  @Test
  fun testAddVoltaConsecutiveFirstIsTwoBars() {
    SAE(EventType.VOLTA, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to 1))
    SAE(EventType.VOLTA, ea(3), paramMapOf(EventParam.END to ea(3), EventParam.NUMBER to 2))
    SVP(EventType.VOLTA, EventParam.NUM_BARS, 2, ez(1))
    SVP(EventType.VOLTA, EventParam.END, true, ez(2))
    SVP(EventType.VOLTA, EventParam.NUM_BARS, 1, ez(3))
  }

  @Test
  fun testDeleteVolta() {
    SAE(EventType.VOLTA, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to 1))
    SDE(EventType.VOLTA, ez(1))
    SVNE(EventType.VOLTA, ez(1))
  }

  @Test
  fun testDeleteVoltaRemovesEnd() {
    SAE(EventType.VOLTA, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to 1))
    SDE(EventType.VOLTA, ez(1))
    SVNE(EventType.VOLTA, ez(2))
  }

  @Test
  fun testSetHardStart() {
    SAE(EventType.VOLTA, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to 1))
    SSP(EventType.VOLTA, EventParam.HARD_START, Coord(20,20), ez(1))
    SVP(EventType.VOLTA, EventParam.HARD_START, Coord(20,20), ez(1))
  }

  @Test
  fun testSetHardStartSecondInGroup() {
    SAE(EventType.VOLTA, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to 1))
    SAE(EventType.VOLTA, ea(3), paramMapOf(EventParam.END to ea(4), EventParam.NUMBER to 2))
    SVE(EventType.VOLTA, ez(3))
    SSP(EventType.VOLTA, EventParam.HARD_START, Coord(20,20), ez(3))
    SVP(EventType.VOLTA, EventParam.HARD_START, Coord(20,20), ez(1))
    SVE(EventType.VOLTA, ez(3))
  }
}