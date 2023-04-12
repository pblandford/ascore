package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.core.area.Coord

import com.philblandford.kscore.engine.types.*
import org.junit.Test

class MetaTest : ScoreTest() {

  @Test
  fun testAddTitle() {
    SAE(EventType.TITLE, eZero(), paramMapOf(EventParam.TEXT to "Hello"))
    SVP(EventType.TITLE, EventParam.TEXT, "Hello", eZero())
  }

  @Test
  fun testSetTitle() {
    SAE(EventType.TITLE, eZero(), paramMapOf(EventParam.TEXT to "Hello"))
    SSP(EventType.TITLE, EventParam.TEXT, "Goodbye", eZero())
    SVP(EventType.TITLE, EventParam.TEXT, "Goodbye", eZero())
  }

  @Test
  fun testAddSubTitle() {
    SAE(EventType.SUBTITLE, eZero(), paramMapOf(EventParam.TEXT to "There"))
    SVP(EventType.SUBTITLE, EventParam.TEXT, "There", eZero())
  }

  @Test
  fun testAddSubTitleKeepsTitle() {
    SAE(EventType.TITLE, eZero(), paramMapOf(EventParam.TEXT to "Hello"))
    SAE(EventType.SUBTITLE, eZero(), paramMapOf(EventParam.TEXT to "There"))
    SVP(EventType.TITLE, EventParam.TEXT, "Hello", eZero())
  }

  @Test
  fun testDeleteTitle() {
    SAE(EventType.TITLE, eZero(), paramMapOf(EventParam.TEXT to "Hello"))
    SDE(EventType.TITLE, eZero())
    SVNE(EventType.TITLE, eZero())
  }

  @Test
  fun testDeleteTitleRetainsSubtitle() {
    SAE(EventType.TITLE, eZero(), paramMapOf(EventParam.TEXT to "Hello"))
    SAE(EventType.SUBTITLE, eZero(), paramMapOf(EventParam.TEXT to "There"))
    SDE(EventType.TITLE, eZero())
    SVP(EventType.SUBTITLE, EventParam.TEXT,  "There", eZero())
  }


  @Test
  fun testSetFont() {
    SAE(EventType.TITLE, eZero(), paramMapOf(EventParam.TEXT to "Hello"))
    SSP(EventType.TITLE, EventParam.FONT, "expression", eZero())
    SVP(EventType.TITLE, EventParam.FONT, "expression", eZero())
  }

  @Test
  fun testSetCoord() {
    SAE(EventType.TITLE, eZero(), paramMapOf(EventParam.TEXT to "Hello"))
    SSP(EventType.TITLE, EventParam.HARD_START, Coord(50,50), eZero())
    SVP(EventType.TITLE, EventParam.HARD_START, Coord(50,50), eZero())
  }

  @Test
  fun testSetCoordToNull() {
    SAE(EventType.TITLE, eZero(), paramMapOf(EventParam.TEXT to "Hello"))
    SSP(EventType.TITLE, EventParam.HARD_START, Coord(50,50), eZero())
    SSP(EventType.TITLE, EventParam.HARD_START, null, eZero())
    SVNP(EventType.TITLE, EventParam.HARD_START, eZero())
  }

}