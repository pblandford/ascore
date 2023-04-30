package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.duration.*
import org.junit.Test

class TieTest : ScoreTest() {

  @Test
  fun testAddTie() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SVP(EventType.TIE, EventParam.DURATION, crotchet(), eav(1).copy(id = 1))
  }

  @Test
  fun testAddTieMarksNoteStart() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.IS_START_TIE, true, eav(1).copy(id = 1))
  }

  @Test
  fun testAddTieMarksNoteEnd() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.END_TIE, crotchet(), eav(1, crotchet()).copy(id = 1))
    SVP(EventType.NOTE, EventParam.IS_END_TIE, true, eav(1, crotchet()).copy(id = 1))
  }

  @Test
  fun testAddTieToMismatchedNotesIsNoop() {
    SMV()
    SMV(63, eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
  }

  @Test
  fun testAddTieChord() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1))
    SVP(EventType.TIE, EventParam.DURATION, crotchet(), eav(1).copy(id = 1))
  }

  @Test
  fun testAddTieChordOtherParamsSame() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    val oldChord = EG().getEvent(EventType.DURATION, eav(1))!!
    SAE(EventType.TIE, eav(1))
    val newChord = EG().getEvent(EventType.DURATION, eav(1))!!
    assertEqual(oldChord.params.minus(EventParam.NOTES), newChord.params.minus(EventParam.NOTES))
  }

  @Test
  fun testAddTieThenChord() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SVE(EventType.TIE, eav(1).copy(id = 1))
    SMV(eventAddress = eav(1, minim()))
    SVE(EventType.TIE, eav(1).copy(id = 1))
    SVP(EventType.TIE, EventParam.IS_END_TIE, true, eav(1, crotchet()).copy(id = 1))
  }

  @Test
  fun testDeleteTie() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDE(EventType.TIE, eav(1).copy(id = 1))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.IS_START_TIE, false, eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.IS_START_TIE, false, eav(1, crotchet()).copy(id = 1))
  }

  @Test
  fun testDeleteNoteTieGone() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDE(EventType.NOTE, eav(1).copy(id = 1))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteEndNoteTieGone() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDE(EventType.NOTE, eav(1, crotchet()).copy(id = 1))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
  }


  @Test
  fun testDeleteEndChordTieGone() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDE(EventType.DURATION, eav(1, crotchet()))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteEndChordTieGoneDifferentDuration() {
    SMV(duration = quaver())
    SMV(eventAddress = eav(1, quaver()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDE(EventType.DURATION, eav(1, quaver()))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteEndNoteEndTieGone() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDE(EventType.NOTE, eav(1, crotchet()).copy(id = 1))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
    SVNE(EventType.TIE, eav(1, crotchet()).copy(id = 1))
  }

  @Test
  fun testReplaceEndChordTieGone() {
    SMV(duration = breve())
    SMV(eventAddress = eav(2), duration = minim())
    SVNE(EventType.TIE, eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteEndNoteRangeEndTieGone() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDR(eav(1, crotchet()), eav(1, crotchet()))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
    SVNE(EventType.TIE, eav(1, crotchet()).copy(id = 1))
  }


  @Test
  fun testDeleteEndChordEndTieGone() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDE(EventType.DURATION, eav(1, crotchet()))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
    SVNE(EventType.TIE, eav(1, crotchet()).copy(id = 1))
  }

  @Test
  fun testDeleteEndChordRangeEndTieGone() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDR(ea(1, crotchet()), ea(1, crotchet()))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
    SVNE(EventType.TIE, eav(1, crotchet()).copy(id = 1))
  }

  @Test
  fun testDeleteTieChordAddress() {
    SMV(duration = breve())
    SDE(EventType.TIE, eav(1))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteTieSegmentAddress() {
    SMV(duration = breve())
    SDE(EventType.TIE, ea(1))
    SVNE(EventType.TIE, eav(1).copy(id = 1))
  }


  @Test
  fun testAddTieFromTuplet() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(duration = quaver(), eventAddress = eav(1, Offset(1, 6)))
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1, Offset(1, 6)))
    SVE(EventType.TIE, eav(1, Offset(1, 6)).copy(id = 1))
  }

  @Test
  fun testDeleteStartNoteEndTieGone() {
    SMV(duration = breve())
    SDE(EventType.DURATION, eav(1))
  }


}