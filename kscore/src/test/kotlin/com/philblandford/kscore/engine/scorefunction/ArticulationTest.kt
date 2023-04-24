package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*

import com.philblandford.kscore.engine.dsl.scoreAllCrotchets
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import org.junit.Test

class ArticulationTest : ScoreTest() {

  @Test
  fun testAddArticulation() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    SVP(EventType.DURATION, EventParam.ARTICULATION, ChordDecoration(false, arrayListOf(ArticulationType.ACCENT)), eav(1))
  }

  @Test
  fun testAddSecondArticulation() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.TENUTO))
    SVP(EventType.DURATION, EventParam.ARTICULATION,
      ChordDecoration(false, arrayListOf(ArticulationType.ACCENT, ArticulationType.TENUTO)), eav(1))
  }

  @Test
  fun testAddSecondArticulationNotUnique() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    SVP(EventType.DURATION, EventParam.ARTICULATION, ChordDecoration(false, arrayListOf(ArticulationType.ACCENT)), eav(1))
  }


  @Test
  fun testAddArticulationRange() {
    sc.setNewScore(scoreAllCrotchets(4))
    SAE(
      EventType.ARTICULATION, eav(1),
      paramMapOf(EventParam.TYPE to ArticulationType.ACCENT), eav(1, crotchet())
    )
    SVP(EventType.DURATION, EventParam.ARTICULATION, ChordDecoration(false, arrayListOf(ArticulationType.ACCENT)), eav(1))
    SVP(EventType.DURATION, EventParam.ARTICULATION,
      ChordDecoration(false, arrayListOf(ArticulationType.ACCENT)), eav(1, crotchet()))
    SVNP(EventType.DURATION, EventParam.ARTICULATION, eav(1, minim()))
  }

  @Test
  fun testDeleteArticulation() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    SDE(EventType.ARTICULATION, eav(1))
    SVNP(EventType.DURATION, EventParam.ARTICULATION, eav(1))
  }

  @Test
  fun testDeleteArticulationRange() {
    SMV()
    SMV(eventAddress = eav(2))
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    SAE(EventType.ARTICULATION, eav(2), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    SDE(EventType.ARTICULATION, eav(1), endAddress = eav(2))
    SVNP(EventType.DURATION, EventParam.ARTICULATION, eav(1))
    SVNP(EventType.DURATION, EventParam.ARTICULATION, eav(2))
  }

  @Test
  fun testGetArticulationAsEvent() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    SVP(EventType.ARTICULATION, EventParam.TYPE, ArticulationType.ACCENT, eav(1))
  }

}