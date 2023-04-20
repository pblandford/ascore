package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.duration.quaver
import com.philblandford.kscore.engine.duration.times
import org.junit.Test

class RepeatBarTest : ScoreTest() {

  @Test
  fun testAddRepeatBar() {
    SMV()
    SAE(EventType.REPEAT_BAR, ea(1), params = paramMapOf(EventParam.NUMBER to 1))
    SVP(EventType.REPEAT_BAR, EventParam.NUMBER, 1, ea(1))
  }

  @Test
  fun testAddRepeatBarRange() {
    SMV()
    SAE(
      EventType.REPEAT_BAR,
      ea(1),
      endAddress = ea(10),
      params = paramMapOf(EventParam.NUMBER to 1)
    )
    repeat(10) {
      SVP(EventType.REPEAT_BAR, EventParam.NUMBER, 1, ea(it + 1))
    }
  }


  @Test
  fun testAddRepeatBarRangeAcrossStaves() {
    SCD(instruments = listOf("Violin", "Viola"))
    SMV()
    SAE(
      EventType.REPEAT_BAR,
      ea(1),
      endAddress = ea(10).copy(staveId = StaveId(2, 1)),
      params = paramMapOf(EventParam.NUMBER to 1)
    )
    repeat(10) { bar ->
      repeat(2) { stave ->
        SVP(
          EventType.REPEAT_BAR,
          EventParam.NUMBER,
          1,
          ea(bar + 1).copy(staveId = StaveId(stave + 1, 1))
        )
      }
    }
  }


  @Test
  fun testAddRepeatBarVoiceIgnored() {
    SMV()
    SAE(EventType.REPEAT_BAR, eav(1), params = paramMapOf(EventParam.NUMBER to 1))
    SVP(EventType.REPEAT_BAR, EventParam.NUMBER, 1, ea(1))
  }

  @Test
  fun testAddRepeatBarOffsetIgnored() {
    SMV()
    SMV(eventAddress = eav(2))
    SAE(EventType.REPEAT_BAR, eav(2, minim()), params = paramMapOf(EventParam.NUMBER to 1))
    SVP(EventType.REPEAT_BAR, EventParam.NUMBER, 1, ea(2))
  }


  @Test
  fun testAddRepeatBarGetEvents() {
    SMV()
    SAE(EventType.REPEAT_BAR, ea(2), params = paramMapOf(EventParam.NUMBER to 1))
    assertEqual("C4:R4:R2", EG().getVoiceMap(eav(2))?.eventString())
  }

  @Test
  fun testAddRepeatBarGetEvent() {
    SMV()
    SAE(EventType.REPEAT_BAR, ea(2), params = paramMapOf(EventParam.NUMBER to 1))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eav(2))
  }

  @Test
  fun testAddRepeatBar2Bars() {
    SMV()
    SMV(eventAddress = eav(2))
    SAE(EventType.REPEAT_BAR, ea(3), params = paramMapOf(EventParam.NUMBER to 2))
    SVP(EventType.REPEAT_BAR, EventParam.NUMBER, 2, ea(3))
  }

  @Test
  fun testAddRepeatBar2BarsGetEvents() {
    SMV()
    SMV(duration = quaver(), eventAddress = eav(2))
    SAE(EventType.REPEAT_BAR, ea(3), params = paramMapOf(EventParam.NUMBER to 2))
    SVP(EventType.DURATION, EventParam.DURATION, crotchet(), eav(3))
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(4))
  }

  @Test
  fun testDeleteRepeatBar() {
    SMV()
    SAE(EventType.REPEAT_BAR, ea(2), params = paramMapOf(EventParam.NUMBER to 1))
    SDE(EventType.REPEAT_BAR, ea(2))
    SVNE(EventType.REPEAT_BAR, ea(2))
  }

  @Test
  fun testAddRepeatBar2BarsOverlapForbidden() {
    SMV()
    SMV(eventAddress = eav(2))
    SAE(EventType.REPEAT_BAR, ea(3), params = paramMapOf(EventParam.NUMBER to 2))
    SAE(EventType.REPEAT_BAR, ea(4), params = paramMapOf(EventParam.NUMBER to 2))
    SVP(EventType.REPEAT_BAR, EventParam.NUMBER, 2, ea(3))
    SVNE(EventType.REPEAT_BAR, ea(4))
  }

  @Test
  fun testAddRepeatBar2OverwritesBar1() {
    SMV()
    SMV(eventAddress = eav(2))
    SAE(EventType.REPEAT_BAR, ea(3), params = paramMapOf(EventParam.NUMBER to 1))
    SAE(EventType.REPEAT_BAR, ea(2), params = paramMapOf(EventParam.NUMBER to 2))
    SVP(EventType.REPEAT_BAR, EventParam.NUMBER, 2, ea(2))
    SVNE(EventType.REPEAT_BAR, ea(3))
  }

  @Test
  fun testAddNotesDeletesRepeatBar() {
    SAE(EventType.REPEAT_BAR, ea(2), params = paramMapOf(EventParam.NUMBER to 1))
    SMV(eventAddress = eav(2))
    SVNE(EventType.REPEAT_BAR, ea(2))
  }

  @Test
  fun testAddNotesDeletesRepeatBarSecondOf2() {
    SAE(EventType.REPEAT_BAR, ea(2), params = paramMapOf(EventParam.NUMBER to 2))
    SMV(eventAddress = eav(3))
    SVNE(EventType.REPEAT_BAR, ea(2))
  }

  @Test
  fun testAddNotesDeletesRepeatBarNotPrevious1() {
    SAE(EventType.REPEAT_BAR, ea(2), params = paramMapOf(EventParam.NUMBER to 1))
    SMV(eventAddress = eav(3))
    SVE(EventType.REPEAT_BAR, ea(2))
  }

  @Test
  fun testAddRepeatBarOverwritesEvents() {
    repeat(4) {
      SMV(eventAddress = eav(2, crotchet() * it))
    }
    SAE(EventType.REPEAT_BAR, ea(2), params = paramMapOf(EventParam.NUMBER to 1))
    SVVM("", eav(2))
  }

  @Test
  fun testAddRepeatBarSparesEventsOtherStaves() {
    SCD(instruments = listOf("Violin", "Viola"))
    repeat(4) {
      SMV(eventAddress = eav(2, crotchet() * it))
    }
    SAE(
      EventType.REPEAT_BAR,
      ea(2).copy(staveId = StaveId(2, 1)),
      params = paramMapOf(EventParam.NUMBER to 1)
    )
    SVVM("C4:C4:C4:C4", eav(2))
  }


  @Test
  fun testAddRepeatBarOverwritesBeams() {
    repeat(8) {
      SMV(duration = quaver(), eventAddress = eav(2, quaver() * it))
    }
    SAE(EventType.REPEAT_BAR, ea(2), params = paramMapOf(EventParam.NUMBER to 1))
    val beams = EG().getBeams(eav(2))
    assert(beams.isEmpty())
  }
}