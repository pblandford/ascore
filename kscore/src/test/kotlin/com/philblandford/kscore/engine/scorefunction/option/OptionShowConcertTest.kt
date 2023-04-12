package com.philblandford.kscore.engine.scorefunction.option

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.pitch.harmony
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class OptionShowConcertTest : ScoreTest() {

  @Test
  fun testShowConcertOptionChangesNote() {
    SCD(instruments = listOf("Trumpet"))
    SMV(74)
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eav(1).copy(id = 1))
  }

  @Test
  fun testShowConcertOptionChangesKeySignature() {
    SCD(instruments = listOf("Trumpet"))
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 0, ez(1))
  }

  @Test
  fun testShowConcertOptionDoesntChangeNoteNonTransposingInstrument() {
    SCD(instruments = listOf("Violin"))
    SMV(74)
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D, Accidental.NATURAL, 5),
      eav(1).copy(id = 1))
  }

  @Test
  fun testShowConcertOptionDoesntChangeKeyNonTransposingInstrument() {
    SCD(instruments = listOf("Violin"))
    SMV(74)
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 0, ez(1))
  }

  @Test
  fun testShowConcertOptionChangesReportedTransposition() {
    SCD(instruments = listOf("Trumpet"))
    SMV(74)
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    assertEqual(0, EG().getInstrument(ea(1))?.transposition)
  }


  @Test
  fun testSetTransposingInstrumentOptionNotesShift() {
    SCD(instruments = listOf("Violin", "Trumpet"))
    SMV()
    SMV(74, eventAddress = eas(1, dZero(), StaveId(2,1)))
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0,3),
      easv(1, dZero(), StaveId(2,1)).copy(id = 1))
  }

  @Test
  fun testUnSetTransposingInstrumentOptionNotesShift() {
    SCD(instruments = listOf("Violin", "Trumpet"))
    SMV()
    SMV(74, eventAddress = eas(1, dZero(), StaveId(2,1)))
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, false)
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0,2),
      easv(1, dZero(), StaveId(2,1)).copy(id = 1))
  }

  @Test
  fun testShowConcertOptionAccidentalNotShown() {
    SCD(instruments = listOf("Trumpet"))
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SMV(72)
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5, false),
      eav(1).copy(id = 1))
  }

  @Test
  fun testShowConcertOptionTenorSax() {
    SCD(instruments = listOf("Tenor Sax"))
    SMV(62)
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 4),
      eav(1).copy(id = 1))
  }

  @Test
  fun testRevertOptionAfterAddingNote() {
    SCD(instruments = listOf("Trumpet"))
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SMV()
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, false)

    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D, Accidental.NATURAL, 5),
      eav(1).copy(id = 1))
  }

  @Test
  fun testRevertOptionAfterAddingNoteTransposingClef() {
    SCD(instruments = listOf("Tenor Sax"))
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SMV()
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, false)

    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D, Accidental.NATURAL, 5),
      eav(1).copy(id = 1))
  }

  @Test
  fun testRevertOptionImmediately() {
    SCD(instruments = listOf("Alto Sax"))
    SMV()
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, false)
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5, true),
      eav(1).copy(id = 1))
  }

  @Test
  fun testConcertKeySignatureAfterAddBars() {
    SCD(instruments = listOf("Trumpet"))
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SAE(EventType.BAR, ez(1), paramMapOf(EventParam.NUMBER to 1))
    assertEqual(0, EG().getKeySignature(ez(1), true))

  }

  @Test
  fun testShowConcertOptionChangesHarmony() {
    SCD(instruments = listOf("Trumpet"))
    SAE(harmony("D")!!.toEvent())
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SVP(EventType.HARMONY, EventParam.TONE, Pitch(NoteLetter.C, Accidental.NATURAL, 0),
      ea(1))
  }

  @Test
  fun testShowConcertOptionChangesHarmonyCorrectEnharmonic() {
    SCD(instruments = listOf("Trumpet"), ks = 4)
    SMV(66, accidental = Accidental.FLAT)
    SAE(harmony("Gb")!!.toEvent())
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SVP(EventType.HARMONY, EventParam.TONE, Pitch(NoteLetter.E, Accidental.NATURAL, 0),
      ea(1))
  }

}