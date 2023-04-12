package com.philblandford.kscore.engine.scorefunction

import assertEqual

import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class NoteEditTest : ScoreTest() {

  @Test
  fun testSetAccidental() {
    SMV()
    SSP(EventType.NOTE, EventParam.ACCIDENTAL, Accidental.SHARP, eav(1).copy(id = 1))
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eav(1).copy(id = 1)
    )
  }


  @Test
  fun testSetNoteHeadType() {
    SMV()
    SSP(EventType.NOTE, EventParam.NOTE_HEAD_TYPE, NoteHeadType.CROSS, eav(1).copy(id = 1))
    SVP(
      EventType.NOTE,
      EventParam.NOTE_HEAD_TYPE,
      NoteHeadType.CROSS,
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testSetNoteHeadTypeRange() {
    SMV()
    SSP(EventType.NOTE, EventParam.NOTE_HEAD_TYPE, NoteHeadType.CROSS, eav(1), eav(2))
    SVP(
      EventType.NOTE,
      EventParam.NOTE_HEAD_TYPE,
      NoteHeadType.CROSS,
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testSetTied() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SSP(EventType.NOTE, EventParam.IS_START_TIE, true, eav(1).copy(id = 1))
    SVP(
      EventType.NOTE,
      EventParam.IS_START_TIE,
      true,
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testSetTiedAddsTie() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SSP(EventType.NOTE, EventParam.IS_START_TIE, true, eav(1).copy(id = 1))
    SVE(EventType.TIE, eav(1).copy(id = 1))
  }

  @Test
  fun testSetTiedSetsNextNoteEndTie() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SSP(EventType.NOTE, EventParam.IS_START_TIE, true, eav(1).copy(id = 1))
    SVP(
      EventType.NOTE,
      EventParam.IS_END_TIE,
      true,
      eav(1, crotchet()).copy(id = 1)
    )  }

  @Test
  fun testSetTiedNoNextNote() {
    SMV()
    SSP(EventType.NOTE, EventParam.IS_START_TIE, true, eav(1).copy(id = 1))
    SVP(
      EventType.NOTE,
      EventParam.IS_START_TIE,
      false,
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testSetTiedMarkerDoesntMove() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    setMarker(ea(4))
    SSP(EventType.NOTE, EventParam.IS_START_TIE, true, eav(1).copy(id = 1))
    assertEqual(ea(4), getMarker())
  }

}