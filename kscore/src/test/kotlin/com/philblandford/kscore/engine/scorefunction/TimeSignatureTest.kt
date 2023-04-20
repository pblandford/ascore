package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.dsl.scoreAllCrotchets
import com.philblandford.kscore.engine.dsl.scoreGrandStave
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.time.TimeSignature
import org.apache.commons.math3.exception.MathArithmeticException
import org.junit.Test

class TimeSignatureTest : ScoreTest() {

  @Test
  fun testAddTimeSignature() {
    SAE(TimeSignature(3, 8).toEvent(), ez(1))
    assertEqual(
      TimeSignature(3, 8), EG().getVoiceMap(
        eav(
          1
        )
      )?.timeSignature
    )
  }

  @Test
  fun testAddTimeSignatureMidScore() {
    SAE(TimeSignature(3, 8).toEvent(), ez(4))
    assertEqual(
      TimeSignature(3, 8), EG().getVoiceMap(
        eav(
          4
        )
      )?.timeSignature
    )
    assertEqual(
      TimeSignature(4, 4), EG().getVoiceMap(
        eav(
          1
        )
      )?.timeSignature
    )
  }

  @Test
  fun testAddTimeSignatureOffsetIgnored() {
    SAE(TimeSignature(3, 4).toEvent(), ez(1, minim()))
    assertEqual(
      TimeSignature(3, 4), EG().getTimeSignature(ez(1))
    )
  }

  @Test
  fun testAddTimeSignatureLonger() {
    SAE(TimeSignature(4, 2).toEvent(), ez(1))
    assertEqual(
      TimeSignature(4, 2), EG().getVoiceMap(
        eav(
          1
        )
      )?.timeSignature
    )
  }

  @Test
  fun testAddTimeSignatureNotesMoved() {
    sc.setNewScore(scoreAllCrotchets(2))
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    assertEqual(4, EG().numBars)
    repeat(4) { num ->
      SVE(
        EventType.DURATION,
        eav(num + 1)
      )
    }
  }

  @Test
  fun testAddTimeSignatureBarsShortened() {
    sc.setNewScore(scoreAllCrotchets(2))
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    assertEqual(4, EG().numBars)
    repeat(4) { num ->
      assertEqual(
        "C4:C4", EG().getVoiceMap(
          eav(
            num + 1
          )
        )?.eventString()
      )
    }
  }

  @Test
  fun testAddTimeSignatureStaveJoinRetained() {
    sc.setNewScore(scoreAllCrotchets(2))
    SAE(
      EventType.STAVE_JOIN,
      ea(1),
      paramMapOf(
        EventParam.END to ea(
          1
        ),
        EventParam.TYPE to StaveJoinType.BRACKET
      )
    )
    SVE(
      EventType.STAVE_JOIN,
      ea(1)
    )
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    SVE(
      EventType.STAVE_JOIN,
      ea(1)
    )
  }

  @Test
  fun testAddTimeSignatureGrandJoinRetained() {
    sc.setNewScore(scoreGrandStave(2))
    SVE(
      EventType.STAVE_JOIN, ea(
        1
      ).copy(staveId = StaveId(1, 0))
    )
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    SVE(
      EventType.STAVE_JOIN, ea(
        1
      ).copy(staveId = StaveId(1, 0))
    )
  }

  @Test
  fun testAddHiddenTimeSignature() {
    val original = EG().getEvent(
      EventType.TIME_SIGNATURE,
      ez(1)
    )
    SAE(
      TimeSignature(3, 8, hidden = true).toHiddenEvent(),
      ez(1)
    )
    assertEqual(
      TimeSignature(3, 8, TimeSignatureType.CUSTOM, true),
      EG().getVoiceMap(eav(1))?.timeSignature
    )
    assertEqual(
      original,
      EG().getEventAt(
        EventType.TIME_SIGNATURE,
        ez(2)
      )?.second
    )
  }

  @Test
  fun testAddHiddenTimeSignatureRest() {
    SAE(
      TimeSignature(3, 8, hidden = true).toHiddenEvent(),
      ez(2)
    )
    SVP(
      EventType.DURATION, EventParam.DURATION, crotchet(1),
      eav(2)
    )
  }

  @Test
  fun testAddHiddenTimeSignatureRestIrregular() {
    SAE(
      TimeSignature(5, 8, hidden = true).toHiddenEvent(),
      ez(2)
    )
    SVP(
      EventType.DURATION, EventParam.TYPE, DurationType.REST,
      eav(2)
    )
    SVP(
      EventType.DURATION, EventParam.DURATION, crotchet(1),
      eav(2)
    )
    SVP(
      EventType.DURATION, EventParam.DURATION, crotchet(),
      eav(2, crotchet(1))
    )
  }

  @Test
  fun testAddHiddenTimeSignatureRestsAddedToNotesHidden() {
    addTs(3, 4, 1)
    SMV(eventAddress = eav(2))
    SVVM("C4:R4:R4", eav(2))
    addTs(4, 4, 2, true)
    SVVM("C4:R4:R2", eav(2))
  }

  @Test
  fun testAddUpbeatTimeSignatureRestIsNotWhole() {
    SAE(
      TimeSignature(1, 8, hidden = true).toHiddenEvent(),
      ez(1)
    )
    SVP(
      EventType.DURATION, EventParam.DURATION, quaver(),
      eav(1)
    )
  }

  @Test
  fun testAddHiddenTimeSignatureNoteInputSuccessful() {
    SAE(
      TimeSignature(1, 4, hidden = true).toHiddenEvent(),
      ez(1)
    )
    repeat(4) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 2, offset = crotchet() * offset))
      }
    }
    repeat(4) { bar ->
      repeat(4) { offset ->
        SVP(
          EventType.DURATION, EventParam.TYPE, DurationType.CHORD,
          eventAddress = eav(bar + 2, offset = crotchet() * offset)
        )
      }
    }
  }

  @Test
  fun testAddTimeSignatureVoiceStaveRemoved() {
    SAE(
      TimeSignature(3, 8).toEvent(),
      eav(2)
    )
    SVE(
      EventType.TIME_SIGNATURE,
      ez(2)
    )
  }

  @Test
  fun testChangeTimeSignatureVoiceMapsCorrect() {
    SMV()
    SAE(TimeSignature(3, 4).toEvent(), ez(1))
    assertEqual(
      "C4:R4:R4", EG().getVoiceMap(
        eav(
          1
        )
      )?.eventString()
    )
  }

  @Test
  fun testChangeTimeSignature15_4to4_4() {
    SAE(
      TimeSignature(15, 4).toEvent(),
      ez(1)
    )
    SMV()
    SAE(TimeSignature(4, 4).toEvent(), ez(1))
    assertEqual(
      "C4:R4:R2", EG().getVoiceMap(
        eav(
          1
        )
      )?.eventString()
    )
  }


  @Test
  fun testAddTimeSignatureAfterSameForbidden() {
    SAE(
      TimeSignature(3, 8).toEvent(),
      eav(1)
    )
    SAE(
      TimeSignature(3, 8).toEvent(),
      eav(2)
    )
    SVNE(
      EventType.TIME_SIGNATURE,
      ez(2)
    )
  }

  @Test
  fun testAddTimeSignatureRemovesLaterSame() {
    SAE(
      TimeSignature(3, 8).toEvent(),
      eav(2)
    )
    SAE(
      TimeSignature(3, 8).toEvent(),
      eav(1)
    )
    SVNE(
      EventType.TIME_SIGNATURE,
      ez(2)
    )
  }

  @Test
  fun testAddTimeSignature2_4to4_4() {
    SCD(TimeSignature(2, 4))
    repeat(4) { bar ->
      repeat(2) { o ->
        SMV(
          eventAddress = eav(
            bar + 1,
            crotchet() * o
          )
        )
      }
    }
    SAE(TimeSignature(4, 4).toEvent(), ez(1))
    repeat(2) { num ->
      SVVM("C4:C4:C4:C4", eav(num + 1))
    }
  }

  @Test
  fun testAddTimeSignatureClefRetained() {
    SAE(
      Event(
        EventType.CLEF,
        paramMapOf(EventParam.TYPE to ClefType.BASS)
      )
    )
    SAE(TimeSignature(3, 8).toEvent(), ez(1))
    SVP(
      EventType.CLEF, EventParam.TYPE, ClefType.BASS,
      ea(1)
    )
  }

  @Test
  fun testAddTimeSignatureNotesPositioned() {
    SAE(Event(EventType.CLEF, paramMapOf(EventParam.TYPE to ClefType.BASS)))
    SMV()
    SAE(TimeSignature(3, 8).toEvent(), ez(1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, -9), eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteTimeSignature() {
    SAE(TimeSignature(3, 8).toEvent(), ez(3))
    SDE(
      EventType.TIME_SIGNATURE,
      ez(3)
    )
    SVNE(
      EventType.TIME_SIGNATURE,
      ez(3)
    )
  }

  @Test
  fun testDeleteTimeSignatureVoiceMapChanged() {
    SAE(TimeSignature(3, 8).toEvent(), ez(3))
    SDE(
      EventType.TIME_SIGNATURE,
      ez(3)
    )
    val vm = EG().getVoiceMap(eav(3))
    assertEqual(TimeSignature(4, 4), vm?.timeSignature)
  }

  @Test
  fun testDeleteTimeSignaturePreviousIsCurrent() {
    SAE(TimeSignature(3, 8).toEvent(), ez(3))
    SDE(
      EventType.TIME_SIGNATURE,
      ez(3)
    )
    assertEqual(
      TimeSignature(4, 4), EG().getTimeSignature(
        ez(
          3
        )
      )
    )
  }

  @Test
  fun testDeleteTimeSignatureNotesReassigned() {
    SAE(TimeSignature(6, 4).toEvent(), ez(3))
    repeat(6) {
      SMV(
        eventAddress = eav(
          3,
          crotchet() * it
        )
      )
    }
    SDE(
      EventType.TIME_SIGNATURE,
      ez(3)
    )
    SVVM("C4:C4:C4:C4", eav(3))
    SVVM("C4:C4:R2", eav(4))
  }

  @Test
  fun testDeleteTimeSignatureBar1IsNoop() {
    SDE(
      EventType.TIME_SIGNATURE,
      ez(1)
    )
    assertEqual(
      TimeSignature(4, 4), EG().getTimeSignature(
        ez(
          1
        )
      )
    )
  }

  @Test
  fun testAddTimeSignatureNotesDivided() {
    SMV(duration = semibreve())
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    SVP(
      EventType.DURATION, EventParam.DURATION, minim(),
      eav(1)
    )
    SVP(
      EventType.DURATION, EventParam.DURATION, minim(),
      eav(2)
    )
  }

  @Test
  fun testAddTimeSignatureTiesAdded() {
    SMV(duration = semibreve())
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    SVE(
      EventType.TIE, eav(
        1
      ).copy(id = 1)
    )
  }

  @Test
  fun testAddTimeSignatureWithTies() {
    SMV(duration = breve())
    SAE(TimeSignature(8, 4).toEvent(), ez(1))
    SVE(
      EventType.TIE, eav(
        1
      ).copy(id = 1)
    )
  }

  @Test
  fun testAddTimeSignatureEndTieMarked() {
    SMV(duration = semibreve())
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    SVP(
      EventType.NOTE, EventParam.IS_END_TIE, true, eav(
        2
      ).copy(id = 1)
    )
  }

  @Test
  fun testAddTimeSignatureEndTieDurationCorrect() {
    SMV(duration = semibreve())
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    SVP(
      EventType.NOTE, EventParam.END_TIE, minim(), eav(
        2
      ).copy(id = 1)
    )
  }

  @Test
  fun testAddTimeSignatureWithTuplet() {
    SAE(
      tuplet(dZero(), 3, 8).toEvent(),
      eav(1)
    )
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    SVVM("R12:R12:R12:R4", eav(1))
  }

  @Test
  fun testAddTimeSignatureWithTwoTuplets() {
    SAE(
      tuplet(dZero(), 3, 8).toEvent(),
      eav(1)
    )
    SAE(
      tuplet(crotchet(), 3, 8).toEvent(),
      eav(1, crotchet())
    )
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    SVVM("R12:R12:R12:R12:R12:R12", eav(1))
  }

  @Test
  fun testAddTimeSignatureWithTupletCrossingBar() {
    SAE(
      tuplet(dZero(), 3, 8).toEvent(),
      eav(1)
    )
    SAE(
      tuplet(crotchet(), 3, 8).toEvent(),
      eav(1, crotchet())
    )
    var thrown = false
    try {
      SAE(
        TimeSignature(5, 16).toEvent(),
        ez(1)
      )
    } catch (e: Exception) {
      thrown = !(e is MathArithmeticException)
    }
    assert(thrown)
  }

  @Test
  fun testAddTimeSignatureLinesAmended() {
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    SVP(EventType.SLUR, EventParam.DURATION, semibreve(), ea(1))
    SVP(EventType.SLUR, EventParam.END, true, ea(3))
    SVP(EventType.SLUR, EventParam.DURATION, semibreve(), ea(3))
  }

  @Test
  fun testAddTimeSignatureBarRemainder() {
    SCD(bars = 2)
    repeat(2) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet() * offset))
      }
    }
    SAE(TimeSignature(3, 4).toEvent(), ez(1))
    repeat(2) { bar ->
      repeat(3) { offset ->
        SVP(
          EventType.DURATION, EventParam.TYPE, DurationType.CHORD,
          eventAddress = eav(bar + 1, crotchet() * offset)
        )
      }
    }
    repeat(2) { offset ->
      SVP(
        EventType.DURATION, EventParam.TYPE, DurationType.CHORD,
        eventAddress = eav(3, crotchet() * offset)
      )
    }
    SVP(
      EventType.DURATION, EventParam.TYPE, DurationType.REST,
      eventAddress = eav(3, crotchet() * 2)
    )
  }

  @Test
  fun testAddTimeSignatureSystemEventsMove() {
    repeat(4) {
      SAE(EventType.REHEARSAL_MARK, ez(it + 1), paramMapOf(EventParam.TEXT to "Who cares"))
    }
    SAE(TimeSignature(2, 4).toEvent(), ez(1))
    repeat(4) {
      SVE(EventType.REHEARSAL_MARK, ez((it * 2) + 1))
      SVNE(EventType.REHEARSAL_MARK, ez((it * 2) + 2))
    }
  }

  @Test
  fun testAddNoteInNewTimeSignatureAfterOldDuration() {
    SAE(TimeSignature(3, 4).toEvent(), ez(1))
    SAE(TimeSignature(4, 4).toEvent(), ez(2))
    SMV(eventAddress = eav(2, minim(1)))
  }

  @Test
  fun testAddCommonTimeSignatureAfterDifferent() {
    SAE(TimeSignature(3, 4).toEvent(), ez(1))
    SAE(TimeSignature(4, 4, TimeSignatureType.COMMON).toEvent(), ez(2))
    SVP(EventType.TIME_SIGNATURE, EventParam.NUMERATOR, 4, ez(2))
    assertEqual(TimeSignature(4, 4, TimeSignatureType.COMMON), EG().getTimeSignature(ez(2)))
  }

  @Test
  fun testAddCommonTimeSignatureWithWrongValuesIsCorrected() {
    SAE(TimeSignature(3, 4).toEvent(), ez(1))
    SAE(TimeSignature(3, 4, TimeSignatureType.COMMON).toEvent(), ez(2))
    SVP(EventType.TIME_SIGNATURE, EventParam.NUMERATOR, 4, ez(2))
    assertEqual(TimeSignature(4, 4, TimeSignatureType.COMMON), EG().getTimeSignature(ez(2)))
  }

  @Test
  fun testAddCutCommonTimeSignatureWithWrongValuesIsCorrected() {
    SAE(TimeSignature(3, 4).toEvent(), ez(1))
    SAE(TimeSignature(3, 4, TimeSignatureType.CUT_COMMON).toEvent(), ez(2))
    SVP(EventType.TIME_SIGNATURE, EventParam.NUMERATOR, 2, ez(2))
    assertEqual(TimeSignature(2, 2, TimeSignatureType.CUT_COMMON), EG().getTimeSignature(ez(2)))
  }

  @Test
  fun testChangeTimeSignatureWithUpbeatBar() {
    SAE(TimeSignature(1, 4, hidden = true).toHiddenEvent(), ez(1))
    SAE(TimeSignature(3,4).toEvent(), ez(1))
    SVPA(EventType.TIME_SIGNATURE, EventParam.NUMERATOR, 3, ez(2))
  }

  @Test
  fun testSetTimeSignatureWithUpbeatBar() {
    SAE(TimeSignature(1, 4, hidden = true).toHiddenEvent(), ez(1))
    SSP(EventType.TIME_SIGNATURE, EventParam.NUMERATOR, 3, ez(1))
    SVPA(EventType.TIME_SIGNATURE, EventParam.NUMERATOR, 3, ez(2))
  }

  @Test
  fun testSetTimeSignatureWithUpbeatBarAffectsWholeScore() {
    SAE(TimeSignature(1, 4, hidden = true).toHiddenEvent(), ez(1))
    SSP(EventType.TIME_SIGNATURE, EventParam.NUMERATOR, 3, ez(1))
    SVPA(EventType.TIME_SIGNATURE, EventParam.NUMERATOR, 3, ez(5))
  }

  private fun addTs(num: Int, den: Int, bar: Int, hidden: Boolean = false) {
    val ts = TimeSignature(num, den, hidden = hidden)

    SAE(
      if (hidden) ts.toHiddenEvent() else ts.toEvent(),
      ez(bar)
    )
  }
}