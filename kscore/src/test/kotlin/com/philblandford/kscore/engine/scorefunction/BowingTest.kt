package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.dsl.scoreAllCrotchets
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.newadder.subadders.ChordDecoration
import org.junit.Test

class BowingTest : ScoreTest() {

  @Test
  fun testAddBowing() {
    SMV()
    SAE(EventType.BOWING, eav(1), paramMapOf(EventParam.TYPE to BowingType.DOWN_BOW))
    SVP(EventType.DURATION, EventParam.BOWING, ChordDecoration(items = arrayListOf(BowingType.DOWN_BOW)), eav(1))
  }

  @Test
  fun testAddBowingRange() {
    sc.setNewScore(scoreAllCrotchets(4))
    SAE(
      EventType.BOWING, eav(1),
      paramMapOf(EventParam.TYPE to BowingType.DOWN_BOW), eav(1, crotchet())
    )
    SVP(EventType.DURATION, EventParam.BOWING, ChordDecoration(items = arrayListOf(BowingType.DOWN_BOW)), eav(1))
    SVP(EventType.DURATION, EventParam.BOWING, ChordDecoration(items = arrayListOf(BowingType.DOWN_BOW)), eav(1, crotchet()))
    SVNP(EventType.DURATION, EventParam.BOWING, eav(1, minim()))
  }

  @Test
  fun testGetBowingAsEvent() {
    SMV()
    SAE(EventType.BOWING, eav(1), paramMapOf(EventParam.TYPE to BowingType.DOWN_BOW))
    SVP(EventType.BOWING, EventParam.TYPE, BowingType.DOWN_BOW, eav(1))
  }

}