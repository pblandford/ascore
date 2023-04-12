package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.paramMapOf

import com.philblandford.kscore.engine.duration.breve
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.semibreve
import org.junit.Test

class PedalTest : ScoreTest() {

  @Test
  fun testAddPedal() {
    SAE(EventType.PEDAL, ea(1), paramMapOf(EventParam.END to ea(2)))
    SVP(EventType.PEDAL, EventParam.DURATION, semibreve(), ea(1))
  }

  @Test
  fun testAddPedalEnd() {
    SAE(EventType.PEDAL, ea(1), paramMapOf(EventParam.END to ea(2)))
    SVP(EventType.PEDAL, EventParam.END, true, ea(2))
  }

  @Test
  fun testAddPedalNoOverlap() {
    SAE(EventType.PEDAL, ea(1), paramMapOf(EventParam.END to ea(3)))
    SAE(EventType.PEDAL, ea(2), paramMapOf(EventParam.END to ea(4)))
    SVP(EventType.PEDAL, EventParam.DURATION, dZero(), ea(1))
    SVP(EventType.PEDAL, EventParam.DURATION, breve(), ea(2))
  }

  @Test
  fun testAddPedalNoOverlapAfter() {
    SAE(EventType.PEDAL, ea(2), paramMapOf(EventParam.END to ea(4)))
    SAE(EventType.PEDAL, ea(1), paramMapOf(EventParam.END to ea(2)))
    SVP(EventType.PEDAL, EventParam.DURATION, semibreve(), ea(1))
    SVP(EventType.PEDAL, EventParam.DURATION, semibreve(), ea(3))
  }
}