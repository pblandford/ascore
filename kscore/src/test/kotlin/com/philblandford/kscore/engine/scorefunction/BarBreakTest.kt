package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.pitch.harmony
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class BarBreakTest : ScoreTest() {

  @Test
  fun testBreakBar() {
    SAE(Event(EventType.BAR_BREAK), ea(1))
    SVE(EventType.PLACE_HOLDER, ea(1, minim()))
  }

  @Test
  fun testBreakBarDuration() {
    SAE(Event(EventType.BAR_BREAK), ea(1))
    SVP(EventType.PLACE_HOLDER, EventParam.REAL_DURATION, minim(), ea(1, minim()))
  }

  @Test
  fun testBreakBarTwice() {
    SAE(Event(EventType.BAR_BREAK), ea(1))
    SAE(Event(EventType.BAR_BREAK), ea(1))
    SVE(EventType.PLACE_HOLDER, ea(1, crotchet()))
    SVE(EventType.PLACE_HOLDER, ea(1, minim()))
  }

  @Test
  fun testBreakBarSecondHalf() {
    SAE(Event(EventType.BAR_BREAK), ea(1))
    SAE(Event(EventType.BAR_BREAK), ea(1, minim()))
    SVE(EventType.PLACE_HOLDER, ea(1, minim()))
    SVE(EventType.PLACE_HOLDER, ea(1, minim(1)))
  }

  @Test
  fun testRemoveBarBreak() {
    SAE(Event(EventType.BAR_BREAK), ea(1))
    SDE(EventType.BAR_BREAK, ea(1))
    SVNE(EventType.PLACE_HOLDER, ea(1))
    SVNE(EventType.PLACE_HOLDER, ea(1, minim()))
  }

  @Test
  fun testRemoveBarBreakRemovesHarmonies() {
    SAE(Event(EventType.BAR_BREAK), ea(1))
    SAE(harmony("C")!!.toEvent(), ea(1, minim()))
    SDE(EventType.BAR_BREAK, ea(1))
    SVNE(EventType.HARMONY, ea(1, minim()))
  }

  @Test
  fun testBreakBar3_4() {
    SAE(TimeSignature(3, 4).toEvent(), ez(1))
    SAE(Event(EventType.BAR_BREAK), ea(1))
    SVE(EventType.PLACE_HOLDER, ea(1, crotchet()))
    SVE(EventType.PLACE_HOLDER, ea(1, minim()))
    assertEqual(3, EG().getEvents(EventType.PLACE_HOLDER)?.size)
  }
}