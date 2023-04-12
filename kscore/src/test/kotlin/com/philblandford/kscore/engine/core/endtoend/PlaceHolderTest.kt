package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea

import com.philblandford.kscore.engine.duration.minim

import core.representation.RepTest
import org.junit.Test

class PlaceHolderTest : RepTest() {

  @Test
  fun testPlaceHolderSegment() {
    SAE(EventType.PLACE_HOLDER, ea(1, minim()))
    RVA("Segment", ea(1, minim()))
  }

  @Test
  fun testLineDrawn() {
    SAE(EventType.PLACE_HOLDER, ea(1, minim()))
    RVA("PlaceHolderLine", ea(1, minim()))
  }

  @Test
  fun testAddBarBreak() {
    SAE(EventType.BAR_BREAK, ea(1))
    RVA("PlaceHolderLine", ea(1))
    RVA("PlaceHolderLine", ea(1, minim()))

  }
}