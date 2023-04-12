package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.area.Coord

import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.tempo.Tempo
import org.junit.Test

class TempoTest : ScoreTest() {

  @Test
  fun testAddTempo() {
    SAE(Tempo(crotchet(), 120).toEvent(), ez(1))
    SVP(EventType.TEMPO, EventParam.BPM, 120, ez(1))
  }

  @Test
  fun testDeleteTempoStartScore() {
    SAE(Tempo(crotchet(), 120).toEvent(), ez(1))
    SDE(EventType.TEMPO, ez(1))
    SVP(EventType.TEMPO, EventParam.HIDDEN, true, ez(1))
  }

  @Test
  fun testDeleteTempoMidScore() {
    SAE(Tempo(crotchet(), 120).toEvent(), ez(5))
    SDE(EventType.TEMPO, ez(5))
    SVNE(EventType.TEMPO, ez(5))
  }

  @Test
  fun testSetTempoPosition() {
    SAE(Tempo(crotchet(), 120).toEvent(), ez(1))
    SSP(EventType.TEMPO, EventParam.HARD_START, Coord(0,-20), ez(1))
    SVP(EventType.TEMPO, EventParam.HARD_START, Coord(0,-20), ez(1))
  }

  @Test
  fun testSetTempoZeroIgnored() {
    SAE(Tempo(crotchet(), 120).toEvent(), ez(1))
    SSP(EventType.TEMPO, EventParam.BPM, 0, ez(1))
    SVP(EventType.TEMPO, EventParam.BPM, 120, ez(1))
  }

}