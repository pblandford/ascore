package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.dsl.scoreAllCrotchets
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.newadder.subadders.ChordDecoration
import org.junit.Test

class FingeringTest : ScoreTest() {

  @Test
  fun testAddFingering() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 1, EventParam.IS_UP to true))
    SVP(EventType.DURATION, EventParam.FINGERING, ChordDecoration(true, arrayListOf(1)), eav(1))
  }

  @Test
  fun testAddFingeringSecond() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 1))
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 2))
    SVP(EventType.DURATION, EventParam.FINGERING, ChordDecoration(false, arrayListOf(1,2)), eav(1))
  }

  @Test
  fun testAddFingeringRange() {
    sc.setNewScore(scoreAllCrotchets(4))
    SAE(
      EventType.FINGERING, eav(1),
      paramMapOf(EventParam.NUMBER to 1), eav(1, crotchet())
    )
    SVP(EventType.DURATION, EventParam.FINGERING, ChordDecoration(false, arrayListOf(1)), eav(1))
    SVP(EventType.DURATION, EventParam.FINGERING, ChordDecoration(false, arrayListOf(1)),
      eav(1, crotchet()))
    SVNP(EventType.DURATION, EventParam.FINGERING, eav(1, minim()))
  }

  @Test
  fun testAddFingeringAbove() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 1, EventParam.IS_UP to true))
    SVP(EventType.DURATION, EventParam.FINGERING, ChordDecoration(true, arrayListOf(1)), eav(1))
  }

  @Test
  fun testAddFingeringBelow() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 1, EventParam.IS_UP to false))
    SVP(EventType.DURATION, EventParam.FINGERING, ChordDecoration(false, arrayListOf(1)), eav(1))
  }

  @Test
  fun testReplaceAboveWithBelow() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 1, EventParam.IS_UP to true))
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 2, EventParam.IS_UP to false))
    SVP(EventType.DURATION, EventParam.FINGERING, ChordDecoration(false, arrayListOf(1,2)), eav(1))
  }

  @Test
  fun testReplaceAboveWithBelowSameNumber() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 1, EventParam.IS_UP to true))
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 1, EventParam.IS_UP to false))
    SVP(EventType.DURATION, EventParam.FINGERING, ChordDecoration(false, arrayListOf(1)), eav(1))
  }

  @Test
  fun testAddFingeringAsList() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to listOf(1,3), EventParam.IS_UP to true))
    SVP(EventType.DURATION, EventParam.FINGERING, ChordDecoration(true, arrayListOf(1,3)), eav(1))
  }

}