package com.philblandford.kscore.clipboard

import assertEqual
import com.philblandford.kscore.api.defaultInstrument
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.dsl.score
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.eventadder.rightOrThrow
import com.philblandford.kscore.engine.time.TimeSignature
import createCrotchets
import org.junit.Test
import com.philblandford.kscore.engine.scorefunction.ScoreTest

class ClipboardTest : ScoreTest() {

  @Test
  fun testPaste() {
    val score = score {
      part {
        stave {
          bar { voiceMap { chord(); chord(); rest(); rest() } }
          bar { voiceMap { } }
        }
      }
    }
    Clipboard.copy(eav(1), eav(1, Duration(3, 4)), score)
    val newScore = Clipboard.paste(eav(2), score).rightOrThrow()
    assertEqual("C4:C4:R4:R4", newScore.getVoiceMap(eav(2))?.eventString())
  }

  @Test
  fun testPasteTwoBars() {
    val score = score {
      part {
        stave {
          bar { voiceMap { chord(); chord(); rest(); rest() } }
          bar { voiceMap { chord(); chord(); chord(); rest() } }
          bar { voiceMap { } }
          bar { voiceMap { } }
        }
      }
    }
    Clipboard.copy(ea(1), ea(2, minim(1)), score)
    val newScore = Clipboard.paste(ea(3), score).rightOrThrow()
    assertEqual("C4:C4:R4:R4", newScore.getVoiceMap(eav(3))?.eventString())
    assertEqual("C4:C4:C4:R4", newScore.getVoiceMap(eav(4))?.eventString())
  }

  @Test
  fun testPasteDifferentOffset() {
    val score = score {
      part {
        stave {
          bar { voiceMap { chord(); chord(); rest(); rest() } }
          bar { voiceMap {} }
          bar { voiceMap {} }
        }
      }
    }
    Clipboard.copy(eav(1), eav(1, minim(1)), score)
    val newScore = Clipboard.paste(eav(2, minim()), score).rightOrThrow()
    assertEqual("R2:C4:C4", newScore.getVoiceMap(eav(2))?.eventString())
    assertEqual("R4:R4:R2", newScore.getVoiceMap(eav(3))?.eventString())
  }

  @Test
  fun testPasteAnyVoice() {
    val score = score {
      part {
        stave {
          bar { voiceMap { chord(); chord(); rest(); rest() } }
          bar { voiceMap { } }
        }
      }
    }
    Clipboard.copy(ea(1), ea(1, Duration(3, 4)), score)
    val newScore = Clipboard.paste(ea(2), score).rightOrThrow()
    assertEqual("C4:C4:R4:R4", newScore.getVoiceMap(eav(2))?.eventString())
  }

  @Test
  fun testPasteMultiStave() {
    val score = score {
      part(defaultInstrument().copy(clefs = listOf(ClefType.TREBLE, ClefType.BASS))) {
        stave {
          bar { voiceMap { chord(); chord(); rest(); rest() } }
          bar { voiceMap { } }
        }
        stave {
          bar { voiceMap { rest(); rest(); chord(); chord() } }
          bar { voiceMap { } }
        }
      }
    }
    Clipboard.copy(eav(1), eav(1, Duration(3, 4)).copy(staveId = StaveId(1, 2)), score)
    val newScore = Clipboard.paste(eav(2), score).rightOrThrow()
    assertEqual("C4:C4:R4:R4", newScore.getVoiceMap(eav(2))?.eventString())
    assertEqual(
      "R4:R4:C4:C4",
      newScore.getVoiceMap(eav(2).copy(staveId = StaveId(1, 2)))?.eventString()
    )
  }

  @Test
  fun testPasteMultiStaveQuavers() {
    SCDG()
    repeat(2) { stave ->
      repeat(8) { offset ->
        SMV(
          duration = quaver(),
          eventAddress = eav(1, quaver() * offset).copy(staveId = StaveId(1, stave + 1))
        )
      }
    }
    Clipboard.copy(eav(1), eav(1, Offset(7, 8)).copy(staveId = StaveId(1, 2)), EG())
    val newScore = Clipboard.paste(eav(2), EG()).rightOrThrow()
    assertEqual("C8:C8:C8:C8:C8:C8:C8:C8", newScore.getVoiceMap(eav(2))?.eventString())
    assertEqual(
      "C8:C8:C8:C8:C8:C8:C8:C8",
      newScore.getVoiceMap(eav(2).copy(staveId = StaveId(1, 2)))?.eventString()
    )
    assertEqual("", newScore.getVoiceMap(eav(3))?.eventString())
    assertEqual(
      "", newScore.getVoiceMap(eav(3).copy(staveId = StaveId(1, 2)))?.eventString()
    )
  }

  @Test
  fun testPasteMultiStaveQuaversNextBarExcluded() {
    SCDG()
    repeat(2) { stave ->
      repeat(2) { bar ->
        repeat(8) { offset ->
          SMV(
            duration = quaver(),
            eventAddress = eav(bar+1, quaver() * offset).copy(staveId = StaveId(1, stave + 1))
          )
        }
      }
    }
    Clipboard.copy(ea(1), ea(1, Offset(7, 8)).copy(staveId = StaveId(1, 2)), EG())
    val newScore = Clipboard.paste(ea(2), EG()).rightOrThrow()
    assertEqual("C8:C8:C8:C8:C8:C8:C8:C8", newScore.getVoiceMap(eav(2))?.eventString())
    assertEqual(
      "C8:C8:C8:C8:C8:C8:C8:C8",
      newScore.getVoiceMap(eav(2).copy(staveId = StaveId(1, 2)))?.eventString()
    )
    assertEqual("", newScore.getVoiceMap(eav(3))?.eventString())
    assertEqual(
      "", newScore.getVoiceMap(eav(3).copy(staveId = StaveId(1, 2)))?.eventString()
    )
  }

  @Test
  fun testCutPaste() {
    val score = score {
      part {
        stave {
          bar { voiceMap { chord(); chord(); rest(); rest() } }
          bar { voiceMap { } }
        }
      }
    }
    Clipboard.cut(eav(1), eav(1, Duration(3, 4)), score)
    val newScore = Clipboard.paste(eav(2), score).rightOrThrow()
    assertEqual("C4:C4:R4:R4", newScore.getVoiceMap(eav(2))?.eventString())
    assertEqual("", newScore.getVoiceMap(eav(1))?.eventString())
  }

  @Test
  fun testCutPasteMultiStave() {
    val score = score {
      part {
        stave {
          bar { voiceMap { chord(); chord(); rest(); rest() } }
          bar { voiceMap { } }
        }
      }
      part {
        stave {
          bar { voiceMap { chord(); chord(); rest(); rest() } }
          bar { voiceMap { } }
        }
      }
    }
    Clipboard.cut(eav(1), easv(1, Duration(3, 4), StaveId(2,1)), score)
    val newScore = Clipboard.paste(eav(2), score).rightOrThrow()
    assertEqual("C4:C4:R4:R4", newScore.getVoiceMap(eav(2))?.eventString())
    assertEqual("C4:C4:R4:R4", newScore.getVoiceMap(easv(2, staveId = StaveId(2,1)))?.eventString())
    assertEqual("", newScore.getVoiceMap(eav(1))?.eventString())
    assertEqual("", newScore.getVoiceMap(easv(1, staveId = StaveId(2,1)))?.eventString())
  }

  @Test
  fun testPaste6_8() {
    SCD(TimeSignature(6, 8))
    repeat(6) {
      SMV(eventAddress = eav(1, quaver().multiply(it)), duration = quaver())
    }

    Clipboard.copy(eav(1), eav(1, Duration(6, 8)), EG())
    val newScore = Clipboard.paste(eav(2), EG()).rightOrThrow()
    assertEqual("C8:C8:C8:C8:C8:C8", newScore.getVoiceMap(eav(2))?.eventString())
  }

  @Test
  fun testPaste6_8BeamsCorrect() {
    SCD(TimeSignature(6, 8))
    repeat(6) {
      SMV(eventAddress = eav(1, quaver().multiply(it)), duration = quaver())
    }

    Clipboard.copy(eav(1), eav(1, Duration(5, 8)), EG())
    val newScore = Clipboard.paste(eav(2), EG()).rightOrThrow()
    assertEqual(
      listOf("8:8:8", "8:8:8", "8:8:8", "8:8:8").toList(),
      newScore.getBeams().getBeamStrings().toList()
    )
  }

  @Test
  fun testPasteTuplet() {
    SCD()
    SAE(tuplet(dZero(), 3, 8).toEvent(), eav(1))
    Clipboard.copy(eav(1), eav(2), EG())
    val newScore = Clipboard.paste(eav(3), EG()).rightOrThrow()
    replaceScore(newScore)
    SVE(EventType.TUPLET, eav(3))
  }

  @Test
  fun testPasteTupletAllNotesCopied() {
    SCD()
    SAE(tuplet(dZero(), 3, 8).toEvent(), eav(1))
    repeat(3) {
      SMV(duration = quaver(), eventAddress = eav(1, Offset(1, 12).multiply(it)))
    }
    Clipboard.copy(eav(1), eav(2), EG())
    val newScore = Clipboard.paste(eav(3), EG()).rightOrThrow()
    replaceScore(newScore)
    repeat(3) {
      SVP(
        EventType.DURATION,
        EventParam.TYPE,
        DurationType.CHORD,
        eav(1, Offset(1, 12).multiply(it))
      )
    }
  }

  @Test
  fun testPasteMidTuplet() {
    SCD()
    SMV(endAddress = eav(1, minim(1)))
    SAE(tuplet(dZero(), 3, 8).toEvent(), eav(2))
    copy(eav(1), eav(1, minim(1)))
    try {
      paste(eav(2, Offset(1, 6)))
    } catch (e: Exception) {
      return
    }
  }

  @Test
  fun testPasteSinglePartMode() {
    SCD(instruments = listOf("Violin", "Viola"))
    SMV()
    SSP(EventType.UISTATE, EventParam.SELECTED_PART, 1)
    copy(eav(1), eav(1, Duration(3, 4)))
    paste(eav(2))
    assertEqual("C4:R4:R2", EG().getVoiceMap(eav(2))?.eventString())
  }

  @Test
  fun testPasteSinglePartModeMultipleNotes() {
    SCD(instruments = listOf("Violin", "Viola"))
    repeat(4) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet() * offset))
      }
    }
    SSP(EventType.UISTATE, EventParam.SELECTED_PART, 1)
    copy(eav(1), eav(5))
    paste(eav(10))
    assertEqual("C4:C4:C4:C4", EG().getVoiceMap(eav(10))?.eventString())
  }


  @Test
  fun testPasteToDifferentPart() {
    SCD(instruments = listOf("Violin", "Violin"))
    SMV()
    copy(eav(1), eav(1, Duration(3, 4)))
    paste(easv(2, dZero(), StaveId(2, 1)))
    assertEqual("C4:R4:R2", EG().getVoiceMap(easv(2, dZero(), StaveId(2, 1)))?.eventString())
  }

  @Test
  fun testPasteToHigherPartPart() {
    SCD(instruments = listOf("Violin", "Violin"))
    SMV()
    SMV(eventAddress = easv(1, dZero(), StaveId(2, 1)))
    copy(easv(1, dZero(), StaveId(2, 1)), easv(2, dZero(), StaveId(2, 1)))
    paste(easv(2, dZero(), StaveId(1, 1)))
    assertEqual("C4:R4:R2", EG().getVoiceMap(easv(1, dZero(), StaveId(1, 1)))?.eventString())
  }


  @Test
  fun testPasteToDifferentClef() {
    SCD(instruments = listOf("Violin", "Cello"))
    SMV(60)
    copy(eav(1), eav(1, Duration(3, 4)))
    paste(easv(2, dZero(), StaveId(2, 1)))
    SVP(
      EventType.NOTE,
      EventParam.POSITION,
      Coord(0, -2),
      easv(2, dZero(), StaveId(2, 1)).copy(id = 1)
    )
  }

  @Test
  fun testPasteToDifferentStave() {
    SCDG()
    SMV(60)
    copy(eav(1), eav(1, Duration(3, 4)))
    paste(easv(2, dZero(), StaveId(1, 2)))
    assertEqual("C4:R4:R2", EG().getVoiceMap(easv(2, dZero(), StaveId(1, 2)))?.eventString())
  }

  @Test
  fun testPasteToDifferentStaveOverflow() {
    SCDG()
    SMV(60)
    SMV(60, eventAddress = easv(1, dZero(), StaveId(1, 2)))
    copy(eav(1), easv(1, Duration(3, 4), StaveId(1, 2)))
    paste(easv(2, dZero(), StaveId(1, 2)))
    assertEqual("C4:R4:R2", EG().getVoiceMap(easv(2, dZero(), StaveId(1, 2)))?.eventString())
  }

  @Test
  fun testPastePastEnd() {
    SCD(bars = 10)
    createCrotchets(4)
    copy(eav(1), eav(4, minim(1)))
    paste(eav(9))
    assertEqual(12, EG().numBars)
    (9..12).forEach {
      assertEqual("C4:C4:C4:C4", EG().getVoiceMap(eav(it))?.eventString())
    }
  }

  @Test
  fun testPasteLyrics() {
    SMV()
    SAE(EventType.LYRIC, ea(1), paramMapOf(EventParam.TEXT to "flob"))
    copy(eav(1), eav(2))
    paste(ea(3))
    SVP(EventType.LYRIC, EventParam.TEXT, "flob", ea(3).copy(id = 1))
  }

  @Test
  fun testPasteHarmony() {
    SMV()
    SAE(EventType.HARMONY, ea(1), paramMapOf(EventParam.TEXT to "C#7"))
    copy(eav(1), eav(2))
    paste(ea(3))
    SVE(EventType.HARMONY, ea(3))
  }

  @Test
  fun testPasteGraceNote() {
    SMV(eventAddress = eagv(1))
    copy(eagv(1), eav(2))
    paste(ea(3))
    SVE(EventType.DURATION, eagv(3))
  }


  @Test
  fun testPasteIntoTransposingInstrument() {
    SCD(instruments = listOf("Violin", "Trumpet"))
    SMV()
    copy(ea(1), ea(2))
    paste(eas(1, dZero(), StaveId(2,1)))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D, Accidental.NATURAL, 5),
      eav(1, dZero()).copy(staveId = StaveId(2,1), id = 1))
  }

  @Test
  fun testPasteIntoTransposingInstrumentTwoStaves() {
    SCD(instruments = listOf("Violin", "Violin", "Trumpet", "Trumpet"))
    SMV()
    SMV(eventAddress = easv(1,2,1))
    copy(ea(1), eas(2,2,1))
    paste(eas(1,3,1))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D, Accidental.NATURAL, 5),
      easv(1,3,1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D, Accidental.NATURAL, 5),
      easv(1,4,1).copy(id = 1))
  }

  @Test
  fun testPasteIntoTransposingInstrumentStaveAbove() {
    SCD(instruments = listOf("Violin", "Trumpet"))
    SMV(eventAddress = eas(1, 2,1))
    copy(eas(1,2,1), eas(2,2,1))
    paste(ea(1))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.B, Accidental.FLAT, 4, true),
      eav(1).copy(id = 1))
  }

  @Test
  fun testPasteIntoTransposingInstrumentTwoStavesAbove() {
    SCD(instruments = listOf("Violin", "Violin", "Trumpet", "Trumpet"))
    SMV(eventAddress = easv(1,3,1))
    SMV(eventAddress = easv(1,4,1))
    copy(eas(1,3,1), eas(2,4,1))
    paste(eas(1,1,1))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.B, Accidental.FLAT, 4, true),
      easv(1,1,1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.B, Accidental.FLAT, 4, true),
      easv(1,2,1).copy(id = 1))
  }

  @Test
  fun testPasteIntoTransposingInstrumentTwoStavesAboveDifferentTranspositions() {
    SCD(instruments = listOf("Alto Sax", "Tenor Sax", "Violin", "Violin"))
    SMV(eventAddress = easv(1,3,1))
    SMV(eventAddress = easv(1,4,1))
    copy(eas(1,3,1), eas(2,4,1))
    paste(eas(1,1,1))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.A, Accidental.NATURAL, 4, false),
      easv(1,1,1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D, Accidental.NATURAL, 5, false),
      easv(1,2,1).copy(id = 1))
  }

  @Test
  fun testPasteIntoTransposingInstrumentShowConcertOption() {
    SCD(instruments = listOf("Violin", "Trumpet"))
    SMV()
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    copy(ea(1), ea(2))
    paste(eas(1, dZero(), StaveId(2,1)))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eav(1, dZero()).copy(staveId = StaveId(2,1), id = 1))
  }


  @Test
  fun testPasteRepeatBar() {
    SMV()
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    copy(eav(1), eav(2))
    paste(ea(3))
    SVE(EventType.REPEAT_BAR, ea(4))
  }

  @Test
  fun testPasteUpbeatBar() {
    var score = score {
      part {
        stave {
          bar { voiceMap { chord() } }
          bar { voiceMap { chord(); chord(); chord(); chord() } }
          bar { voiceMap { } }
          bar { voiceMap { } }
        }
      }
    }
    score = score.addEvent(TimeSignature(1,4).toHiddenEvent(), ez(1))!!
    Clipboard.copy(ea(1), ea(2, minim(1)), score)
    val newScore = Clipboard.paste(ea(3), score).rightOrThrow()
    assertEqual("C4:C4:C4:C4", newScore.getVoiceMap(eav(3))?.eventString())
    assertEqual("C4:R4:R2", newScore.getVoiceMap(eav(4))?.eventString())
  }


  @Test
  fun testPasteLongTrill() {
    SCD()
    SMV(duration = semibreve())
    SAE(EventType.LONG_TRILL, eav(1), paramMapOf(), eav(1))
    SVE(EventType.LONG_TRILL, eav(1))

    Clipboard.copy(eav(1), eav(2), EG())
    val newScore = Clipboard.paste(eav(3), EG()).rightOrThrow()
    replaceScore(newScore)
    SVE(EventType.LONG_TRILL, eav(3))
  }


  @Test
  fun testPasteSlur() {
    SCD()
    SMV(duration = semibreve())
    SMV(duration = semibreve(), eventAddress = eav(2))
    SAE(EventType.SLUR, eav(1), paramMapOf(), eav(2))
    SVE(EventType.SLUR, eav(1))

    Clipboard.copy(eav(1), eav(2), EG())
    val newScore = Clipboard.paste(eav(3), EG()).rightOrThrow()
    replaceScore(newScore)
    SVE(EventType.SLUR, eav(3))
  }



  private fun copy(start: EventAddress, end: EventAddress) {
    Clipboard.copy(start, end, EG())
  }

  private fun cut(start: EventAddress, end: EventAddress) {
    Clipboard.copy(start, end, EG())
  }

  private fun paste(eventAddress: EventAddress) {
    val score = Clipboard.paste(eventAddress, EG()).rightOrThrow()
    replaceScore(score)
  }
}