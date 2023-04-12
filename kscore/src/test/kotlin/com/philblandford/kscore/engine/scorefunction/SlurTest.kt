package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.area.Coord

import com.philblandford.kscore.engine.duration.*
import grace
import org.junit.Test

class SlurTest : ScoreTest() {

  @Test
  fun testAddSlur() {
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    SVP(EventType.SLUR, EventParam.DURATION, semibreve(), ea(1))
  }

  @Test
  fun testAddSlurDown() {
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to false))
    SVP(EventType.SLUR, EventParam.IS_UP, false, ea(1).copy(id = 1))
  }

  @Test
  fun testAddTwoSlursDown() {
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to false))
    SAE(EventType.SLUR, ea(3), paramMapOf(EventParam.END to ea(4), EventParam.IS_UP to false))
    SVP(EventType.SLUR, EventParam.IS_UP, false, ea(1).copy(id = 1))
    SVP(EventType.SLUR, EventParam.IS_UP, false, ea(3).copy(id = 1))
  }

  @Test
  fun testAddTwoSlursDownReverseOrder() {
    SAE(EventType.SLUR, ea(3), paramMapOf(EventParam.END to ea(4), EventParam.IS_UP to false))
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to false))
    SVP(EventType.SLUR, EventParam.IS_UP, false, ea(1).copy(id = 1))
    SVP(EventType.SLUR, EventParam.IS_UP, false, ea(3).copy(id = 1))
  }

  @Test
  fun testAddTwoSlursDownReverseOrderOverQuavers() {
    repeat(2) { bar ->
      repeat(8) { q ->
        SMV(eventAddress = eav(bar + 1, quaver() * 1))
      }
    }
    SAE(EventType.SLUR, ea(2), paramMapOf(EventParam.END to ea(2, crotchet(1)), EventParam.IS_UP to false))
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(1, crotchet(1)), EventParam.IS_UP to false))
    SVP(EventType.SLUR, EventParam.IS_UP, false, ea(1).copy(id = 1))
    SVP(EventType.SLUR, EventParam.IS_UP, false, ea(2).copy(id = 1))
  }

  @Test
  fun testAddTwoSlursDownReverseOrderOverQuaversOffset() {
    repeat(2) { bar ->
      repeat(8) { q ->
        SMV(eventAddress = eav(bar + 1, quaver() * 1))
      }
    }
    SAE(EventType.SLUR, ea(2, quaver()), paramMapOf(EventParam.END to ea(2, crotchet()), EventParam.IS_UP to false))
    SAE(EventType.SLUR, ea(1, quaver()), paramMapOf(EventParam.END to ea(1, crotchet()), EventParam.IS_UP to false))
    SVP(EventType.SLUR, EventParam.IS_UP, false, ea(1, quaver()).copy(id = 1))
    SVP(EventType.SLUR, EventParam.IS_UP, false, ea(2, quaver()).copy(id = 1))
  }

  @Test
  fun testSlurSetHardMid() {
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.DURATION to semibreve(), EventParam.IS_UP to true,
      EventParam.HARD_MID to Coord(0,20)))
    SVP(EventType.SLUR, EventParam.HARD_MID, Coord(0,20), ea(1))
  }

  @Test
  fun testAddSlurVoiceRemoved() {
    SAE(EventType.SLUR, eav(1), paramMapOf(EventParam.END to eav(2), EventParam.IS_UP to true))
    SVP(EventType.SLUR, EventParam.DURATION, semibreve(), ea(1))
  }

  @Test
  fun testAddSlurUpAndDOwn() {
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to false))
    SVP(EventType.SLUR, EventParam.DURATION, semibreve(), ea(1))
    SVP(EventType.SLUR, EventParam.DURATION, semibreve(), ea(1).copy(id = 1))
  }

  @Test
  fun testAddSlurCreatesEndPoint() {
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    SVP(EventType.SLUR, EventParam.END, true, ea(2))
  }

  @Test
  fun testDeleteSlur() {
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    SDE(EventType.SLUR, ea(1))
    SVNE(EventType.SLUR, ea(1))
  }

  @Test
  fun testDeleteSlurRemovesEnd() {
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    SDE(EventType.SLUR, ea(1))
    SVNE(EventType.SLUR, ea(2))
  }

  @Test
  fun testAddSlurBackwardRange() {
    SAE(EventType.SLUR, ea(2), paramMapOf(EventParam.END to ea(1), EventParam.IS_UP to true))
    SVP(EventType.SLUR, EventParam.DURATION, semibreve(), ea(1))
  }

  @Test
  fun testSetSlurDown() {
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    SSP(EventType.SLUR, EventParam.IS_UP, false, ea(1))
    SVP(EventType.SLUR, EventParam.IS_UP, false, ea(1).copy(id = 1))
    SVNE(EventType.SLUR, ea(1))
  }

  @Test
  fun testAddSlurGraceNotes() {
    grace()
    grace()
    SAE(EventType.SLUR, eag(1), paramMapOf(EventParam.END to eag(1, graceOffset = semiquaver()), EventParam.IS_UP to true))
    SVP(EventType.SLUR, EventParam.DURATION, dZero(), eag(1))
    SVP(EventType.SLUR, EventParam.GRACE_OFFSET_END, semiquaver(), eag(1))
    SVP(EventType.SLUR, EventParam.DURATION, dZero(), eag(1, graceOffset = semiquaver()))
  }


  @Test
  fun testAddSlurGraceNoteToNormal() {
    SMV()
    grace()
    grace()
    SAE(EventType.SLUR, eag(1), paramMapOf(EventParam.END to ea(1), EventParam.IS_UP to true))
    SVP(EventType.SLUR, EventParam.DURATION, dZero(), eag(1))
    SVNP(EventType.SLUR, EventParam.GRACE_OFFSET_END, eag(1))
    SVP(EventType.SLUR, EventParam.DURATION, dZero(), ea(1))
  }


  @Test
  fun testAddSlurGraceNotesSetCoord() {
    grace()
    grace()
    SAE(EventType.SLUR, eag(1), paramMapOf(EventParam.END to eag(1, graceOffset = semiquaver()), EventParam.IS_UP to true))
    SSP(EventType.SLUR, EventParam.HARD_MID, Coord(0,-20), eag(1))
    SVP(EventType.SLUR, EventParam.DURATION, dZero(), eag(1))
    SVP(EventType.SLUR, EventParam.GRACE_OFFSET_END, semiquaver(), eag(1))
    SVP(EventType.SLUR, EventParam.DURATION, dZero(), eag(1, graceOffset = semiquaver()))
  }

}