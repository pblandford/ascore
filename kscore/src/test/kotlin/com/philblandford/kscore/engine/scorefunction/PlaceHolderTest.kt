package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea

import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.pitch.harmony
import org.junit.Test

class PlaceHolderTest : ScoreTest() {

  @Test
  fun testPlaceHolder() {
    SAE(Event(EventType.PLACE_HOLDER), ea(1))
    SVE(EventType.PLACE_HOLDER, ea(1))
  }

  @Test
  fun testPlaceHolderMidBar() {
    SAE(Event(EventType.PLACE_HOLDER), ea(1, minim()))
    SVP(EventType.PLACE_HOLDER, EventParam.REAL_DURATION, minim(), ea(1, minim()))
  }

  @Test
  fun testPlaceHolderAsSegment() {
    SAE(Event(EventType.PLACE_HOLDER), ea(1, minim()))
    assertEqual(ea(1, minim()), EG().getNextStaveSegment(ea(1)))
  }

  @Test
  fun testDeletePlaceHolder() {
    SAE(Event(EventType.PLACE_HOLDER), ea(1))
    SDE(EventType.PLACE_HOLDER, ea(1))
    SVNE(EventType.PLACE_HOLDER, ea(1))
  }

  @Test
  fun testDeletePlaceHolderRemovesHarmoniesMidBar() {
    SAE(Event(EventType.BAR_BREAK), ea(1))
    SAE(harmony("C7")!!.toEvent(), ea(1, minim()))
    SDE(EventType.BAR_BREAK, ea(1))
    SVNE(EventType.HARMONY, ea(1, minim()))
  }

}