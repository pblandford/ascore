package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.pitch.harmony
import com.philblandford.kscore.engine.time.TimeSignature
import grace
import org.junit.Test

class DeleteTest : ScoreTest() {

  @Test
  fun testDeleteDurationEvent() {
    SMV()
    SDE(EventType.DURATION, eav(1))
    SVNE(EventType.DURATION, eav(1))
  }

  @Test
  fun testDeleteDurationEventLastCrotchet() {
    repeat(4) {
      SMV(eventAddress = eav(1, crotchet() * it))
    }
    SDE(EventType.DURATION, eav(1, crotchet()*3))
    SVVM("C4:C4:C4:R4", eav(1))
  }



  @Test
  fun testDeleteDurationEventRange() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SDE(EventType.DURATION, eav(1), endAddress = eav(1, crotchet()))
    SVNE(EventType.DURATION, eav(1))
  }

  @Test
  fun testDeleteDurationEventRangeStartWithRest() {
    SAE(rest(crotchet()))
    SMV(eventAddress = eav(1, crotchet()))
    SMV(eventAddress = eav(1, minim()))
    SDE(EventType.DURATION, eav(1), endAddress = eav(1, minim(1)))
    repeat(4) {
      SVNE(EventType.DURATION, eav(1, crotchet().multiply(it)))
    }
  }


  @Test
  fun testDeleteDurationEventRangeTwoBars() {
    SMV()
    SMV(eventAddress = eav(2))
    SDE(EventType.DURATION, eav(1), endAddress = eav(2))
    SVNE(EventType.DURATION, eav(1))
    SVNE(EventType.DURATION, eav(2))
  }

  @Test
  fun testDeleteDurationEventRangeTwoVoices() {
    SMV()
    SMV(eventAddress = eav(1, voice = 2))
    SDE(EventType.DURATION, ea(1), endAddress = ea(2))
    SVNE(EventType.DURATION, eav(1))
    SVNE(EventType.DURATION, eav(1, voice = 2))
  }

  @Test
  fun testDeleteDurationEventRangeTwoStaves() {
    SCDG()
    repeat(2) { stave ->
      repeat(4) { offset ->
        SMV(eventAddress = eas(1, crotchet().multiply(offset), StaveId(1, stave + 1)))
      }
    }
    SDE(EventType.DURATION, eav(1), endAddress = eas(1, minim(1), StaveId(1, 2)))
    repeat(2) { stave ->
      repeat(4) { offset ->
        SVNE(EventType.DURATION, eas(1, crotchet().multiply(offset), StaveId(1, stave + 1)))
      }
    }
  }

  @Test
  fun testDeleteDurationEventRangeTwoStavesLowerNoteLonger() {
    SCDG()
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SMV(duration = minim(), eventAddress = easv(1, dZero(), StaveId(1, 2)))
    SDE(EventType.DURATION, eav(1), endAddress = easv(1, dZero(), StaveId(1, 2)))
    repeat(2) { stave ->
      repeat(4) { offset ->
        SVNE(EventType.DURATION, easv(1, crotchet().multiply(offset), StaveId(1, stave)))
      }
    }
  }

  @Test
  fun testDeleteDurationEventRangeUpbeatRest() {
    SAE(TimeSignature(1, 4, hidden = true).toEvent(), ez(1))
    SMV()
    SMV(eventAddress = eav(2))
    SDE(EventType.DURATION, eav(1), endAddress = eav(2))
    SVNE(EventType.DURATION, eav(1))
  }

  @Test
  fun testDeleteRange() {
    SMV()
    SMV(eventAddress = eav(2))
    SDR(ea(1), ea(2))
    SVNE(EventType.DURATION, ea(1))
    SVNE(EventType.DURATION, ea(2))
  }

  @Test
  fun testDeleteRangeAllCrotchets() {
    repeat(4) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)))
      }
    }
    SDR(ea(1), ea(5))
    repeat(4) { bar ->
      repeat(4) { offset ->
        SVNE(EventType.DURATION, eav(bar + 1, crotchet().multiply(offset)))
      }
    }
  }

  @Test
  fun testDeleteRangeEmptyBarSecondStave() {
    SCD(instruments = listOf("Violin", "Viola"))
    repeat(4) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)))
      }
    }
    SDR(ea(1), eas(4, dZero(), StaveId(2,1)))
    repeat(4) { bar ->
      repeat(4) { offset ->
        SVNE(EventType.DURATION, eav(bar + 1, crotchet().multiply(offset)))
      }
    }
  }

  @Test
  fun testDeleteRangeAllCrotchets9_4() {
    SAE(TimeSignature(9, 4).toEvent(), ez(1))
    repeat(9) { offset ->
      SMV(eventAddress = eav(1, crotchet().multiply(offset)))
    }
    SDR(ea(1), ea(2))
    repeat(9) { offset ->
      SVNE(EventType.DURATION, eav(1, crotchet().multiply(offset)))
    }
  }

  @Test
  fun testDeleteRangeFillsRests() {
    repeat(4) { offset ->
      SMV(eventAddress = eav(1, crotchet().multiply(offset)))
    }
    SDR(ea(1), ea(1, minim()))
    assertEqual("R2:R4:C4", EG().getVoiceMap(eav(1))?.eventString())
  }

  @Test
  fun testDeleteRangeSparesInitialClef() {
    repeat(4) { offset ->
      SMV(eventAddress = eav(1, crotchet().multiply(offset)))
    }
    SDR(ea(1), ea(1, minim()))
    SVE(EventType.CLEF, ea(1))
  }

  @Test
  fun testDeleteRangeDeletesClef() {
    SAE(EventType.CLEF, ea(2), paramMapOf(EventParam.TYPE to ClefType.BASS))
    SDR(ea(1), ea(3))
    SVNE(EventType.CLEF, ea(2))
  }

  @Test
  fun testDeleteRangeDeletesClefNotesMove() {
    SAE(EventType.CLEF, ea(2), paramMapOf(EventParam.TYPE to ClefType.BASS))
    SMV(60, eventAddress = eav(4))
    SDR(ea(1), ea(3))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 10), eav(4).copy(id = 1))
  }

  @Test
  fun testDeleteRangeDeletesTuplet() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8
      )
    )
    SDR(ea(1), ea(2))
    SVNE(EventType.TUPLET, eav(1))
  }

  @Test
  fun testDeleteRangeDeletesSlurs() {
    SAE(EventType.SLUR, ea(2), paramMapOf(EventParam.END to ea(3),
      EventParam.IS_UP to true))
    SDR(ea(1), ea(3))
    SVNE(EventType.SLUR, ea(2))
  }

  @Test
  fun testDeleteRangeDeletesHarmonies() {
    SAE(harmony("C")!!.toEvent(), ea(2))
    SDR(ea(1), ea(3))
    SVNE(EventType.HARMONY, ea(2))
  }

  @Test
  fun testDeleteRangeConsolidatesRests() {
    repeat(5) {
      SMV(duration = quaver(), eventAddress = eav(1, quaver()*it))
    }
    SDR(ea(1), ea(1, crotchet(1)))
    SVVM("R2:C8:R8:R4", eav(1))
  }

  @Test
  fun testDeleteRangePartOfTuplet() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8
      )
    )
    repeat(3) { offset ->
      SMV(duration = quaver(), eventAddress = eav(1, Offset(1, 12) * offset))
    }
    SDR(ea(1, Offset(1, 12)), ea(2))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eav(1))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eav(1, Offset(1, 12)))
  }

  @Test
  fun testDeleteRangeStartOfTuplet() {
    SAE(
      EventType.TUPLET, eav(1, crotchet()), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8
      )
    )
    repeat(3) { offset ->
      SMV(
        duration = quaver(),
        eventAddress = eav(1, crotchet() + (Offset(1, 12) * offset))
      )
    }
    SDR(ea(1), ea(1, crotchet() + Offset(1, 12)))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eav(1, crotchet()))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eav(1, crotchet() + Offset(1, 6)))
  }

  @Test
  fun testDeleteRangeSparesInstrument() {
    repeat(2) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)))
      }
    }
    SDR(ea(1), ea(5))
    SVE(EventType.INSTRUMENT, ea(1))
  }

  @Test
  fun testDeleteRangeSparesOption() {
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    repeat(2) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)))
      }
    }
    SDR(ea(1), ea(5))
    assertEqual(true,  EG().getOption(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT))
  }

  @Test
  fun testDeleteRangeSparesRehearsalMarkOutsideRange() {
    SAE(EventType.REHEARSAL_MARK, ez(1), paramMapOf(EventParam.TEXT to "A"))
    SVE(EventType.REHEARSAL_MARK, ez(1))

    repeat(2) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)))
      }
    }
    SDR(ea(2), ea(4))
    SVE(EventType.REHEARSAL_MARK, ez(1))
  }

  @Test
  fun testDeleteRangeSparesRehearsalMarkInsideRange() {
    SAE(EventType.REHEARSAL_MARK, ez(2), paramMapOf(EventParam.TEXT to "A"))
    SVE(EventType.REHEARSAL_MARK, ez(2))

    repeat(2) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)))
      }
    }
    SDR(ea(2), ea(4))
    SVE(EventType.REHEARSAL_MARK, ez(2))
  }

  @Test
  fun testAddNoteAfterDeleteRange() {
    repeat(2) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)))
      }
    }
    SDR(ea(1), ea(5))
    SMV()
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eav(1))
  }

  @Test
  fun testDeleteV2RestAffectsV1Stem() {
    SMV(72)
    SAE(rest(minim()), eav(1, dZero(), 2))
    SDE(EventType.DURATION, eav(1, dZero(), 2))
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, false, eav(1))
  }

  @Test
  fun testDeleteLastV2RestRemovesVoiceMap() {
    SAE(rest(minim()), eav(1, dZero(), 2))
    SDE(EventType.DURATION, eav(1, dZero(), 2))
    SDE(EventType.DURATION, eav(1, minim(), 2))
    assert(EG().getVoiceMap(eav(1, dZero(), 2)) == null)
  }

  @Test
  fun testDeleteLastV2RestRetainsV1VoiceMap() {
    SAE(rest(minim()), eav(1, dZero(), 2))
    SDE(EventType.DURATION, eav(1, dZero(), 2))
    SDE(EventType.DURATION, eav(1, minim(), 2))
    assert(EG().getVoiceMap(eav(1, dZero(), 1)) != null)
  }

  @Test
  fun testDeleteV2RangeWithRests() {
    repeat(4) { bar ->
      repeat(8) { offset  ->
        if (offset != 4) {
          SMV(duration = quaver(), eventAddress = eav(bar + 1, quaver() * offset, 2))
        }
      }
    }
    SDR(ea(1), ea(1, quaver()*7))
    SVVM("", eav(1, voice = 1))
    assert(EG().getVoiceMap(eav(1, voice = 2)) == null)
  }

  @Test
  fun testDeleteRangeGrace() {
    grace()
    grace()
    SDR(eag(1), eag(1, graceOffset = semiquaver()))
    SVNE(EventType.DURATION, eag(1))
    SVNE(EventType.DURATION, eag(1, graceOffset = semiquaver()))
  }

  @Test
  fun testDeleteRangeStrayTiesRemoved() {
    SMV(duration = breve())
    SDR(eav(1), eav(1))
    SVP(EventType.NOTE, EventParam.IS_START_TIE, false, eav(2).copy(id = 1))
  }

  @Test
  fun testDeleteRangeStrayTiesRemovedSameBar() {
    SMV(duration = minim())
    SMV(duration = minim(), eventAddress = eav(1, minim()))
    SDR(eav(1), eav(1))
    SVP(EventType.NOTE, EventParam.IS_START_TIE, false, eav(1, minim()).copy(id = 1))
  }
}