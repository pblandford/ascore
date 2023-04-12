package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.duration.*
import org.junit.Test

class NoteDurationTest : ScoreTest() {

  @Test
  fun testChangeNoteDuration() {
    SMV()
    SSP(EventType.DURATION, EventParam.DURATION, minim(), eav(1).copy(id = 1))
    SVP(EventType.DURATION, EventParam.DURATION, minim(), eav(1))
  }

  @Test
  fun testChangeNoteDurationReal() {
    SMV()
    SSP(EventType.DURATION, EventParam.DURATION, minim(), eav(1).copy(id = 1))
    SVP(EventType.DURATION, EventParam.REAL_DURATION, minim(), eav(1))
  }


  @Test
  fun testChangeNoteDurationChangesNotes() {
    SMV()
    SSP(EventType.DURATION, EventParam.DURATION, minim(), eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.DURATION, minim(), eav(1).copy(id = 1))
  }


  @Test
  fun testChangeNoteDurationTuplet() {
    SAE(tuplet(dZero(), 3, 8).toEvent(), eav(1))
    SMV(duration = quaver())
    SSP(EventType.DURATION, EventParam.DURATION, crotchet(), eav(1).copy(id = 1))
    SVP(EventType.DURATION, EventParam.DURATION, crotchet(), eav(1))
    SVP(EventType.DURATION, EventParam.REAL_DURATION, Duration(1,6), eav(1))
  }
}