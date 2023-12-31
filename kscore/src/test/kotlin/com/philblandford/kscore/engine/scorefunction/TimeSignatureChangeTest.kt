package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.breve
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.duration.semibreve
import com.philblandford.kscore.engine.duration.times
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class TimeSignatureChangeTest : ScoreTest() {

  @Test
  fun testChangeTSEmptyScore() {
    setTs(3, 4)
    verifyTs(3, 4)
  }

  @Test
  fun testChangeTsAllCrotchets() {
    repeat(EG().numBars) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet() * offset))
      }
    }
    setTs(3, 4)
    verifyTs(3, 4)
    repeat(EG().numBars) { bar ->
      repeat(3) { offset ->
        SVP(
          EventType.DURATION,
          EventParam.DURATION,
          crotchet(),
          eventAddress = eav(bar + 1, crotchet() * offset)
        )
      }
    }
  }

  @Test
  fun testChangeTsWithTies() {
    repeat(EG().numBars) { bar ->
      SMV(duration = semibreve(), eventAddress = eav(bar + 1, minim()))
    }
    setTs(3, 4)
    verifyTs(3, 4)
  }

  @Test
  fun testChange3_4To4_4TsWithTies() {
    setTs(3, 4)
    repeat(EG().numBars) { bar ->
      SMV(
        duration = minim(),
        eventAddress = EG().getParam(EventType.UISTATE, EventParam.MARKER_POSITION)!!
      )
    }
    setTs(4,4)
    verifyTs(4, 4)
  }

  @Test
  fun testChangeTsRestStartBar() {
    setTs(3, 4)
    SAE(rest(crotchet()))
    SMV(duration = minim(), eventAddress = eav(1, crotchet()))
    setTs(4,4)
    verifyTs(4, 4)
  }

  @Test
  fun testChangeTsRestStartBarMidScore() {
    setTs(3, 4)
    SAE(rest(crotchet()), eventAddress = eav(20))
    SMV(duration = minim(), eventAddress = eav(20, crotchet()))
    setTs(4,4)
    verifyTs(4, 4)
  }

  @Test
  fun testChangeTestUpbeatBar() {
    SCD(upbeat = TimeSignature(1,4))
    setTs(3,4)
    verifyTs(1,4)
    verifyTs(3,4, ez(2))
  }

  @Test
  fun testChangeTestUpbeatBarWithContent() {
    SCD(upbeat = TimeSignature(1,4))
    SMV()
    repeat(2) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 2, crotchet() * offset))
      }
    }
    setTs(3,4)
    SAE(TimeSignature(3, 4).toHiddenEvent(), ez(1))
    verifyTs(1,4)
    verifyTs(3,4, ez(2))
    SVE(EventType.HIDDEN_TIME_SIGNATURE, ez(1))
    SVNE(EventType.HIDDEN_TIME_SIGNATURE, ez(2))
  }

  @Test
  fun testChangeTimeSignatureDifferentLater() {
    SMV()
    repeat(8) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 2, crotchet() * offset))
      }
    }
    setTs(3,4, ez(5))
    setTs(2,4, ez(1))
    verifyTs(2,4,ez(1))
    verifyTs(3,4,ez(9))
  }

  @Test
  fun testChangeTimeSignatureDifferentVoiceMapTS() {
    SMV()
    repeat(8) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 2, crotchet() * offset))
      }
    }
    setTs(3,4, ez(5))
    setTs(2,4, ez(1))
    var vm = EG().getVoiceMap(eav(9))!!
    assertEqual(TimeSignature(3,4), vm.timeSignature)
    vm = EG().getVoiceMap(eav(8))!!
    assertEqual(TimeSignature(2,4), vm.timeSignature)
  }

  @Test
  fun testChangeTSWithLineEvent() {
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to 1), ea(2))
    setTs(3, 4)
    SVP(EventType.OCTAVE, EventParam.DURATION, semibreve())
  }

  @Test
  fun testChangeTSWithLineEventZeroDuration() {
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to 1), ea(1))
    setTs(3, 4)
    SVP(EventType.OCTAVE, EventParam.DURATION, dZero())
  }

  @Test
  fun testChangeTSWithLineEventWithCrotchets() {
    SCD(timeSignature = TimeSignature(8,4))
    (1..2).forEach { bar ->
      (0..7).forEach { offset ->
        SMV(eventAddress = eav(bar, crotchet() * offset))
      }
    }
    SAE(EventType.OCTAVE, ea(2), paramMapOf(EventParam.NUMBER to 1), ea(2, crotchet()* 7))
    setTs(9, 4)
    SVP(EventType.OCTAVE, EventParam.DURATION, crotchet() * 7, ea(2, crotchet()))
  }

  private fun setTs(num: Int, den: Int, eventAddress: EventAddress = ez(1)) {
    SAE(TimeSignature(num, den).toEvent(), eventAddress)
  }

  private fun verifyTs(num: Int, den: Int, eventAddress: EventAddress = ez(1)) {
    SVP(EventType.TIME_SIGNATURE, EventParam.NUMERATOR, num, eventAddress)
    SVP(EventType.TIME_SIGNATURE, EventParam.DENOMINATOR, den, eventAddress)
  }
}