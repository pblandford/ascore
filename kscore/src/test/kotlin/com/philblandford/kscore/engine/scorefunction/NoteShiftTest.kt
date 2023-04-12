package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.area.Coord


import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.newadder.subadders.ChordDecoration
import org.junit.Test

class NoteShiftTest : ScoreTest() {

  @Test
  fun testShiftNote() {
    SMV()
    SAE(
      EventType.NOTE_SHIFT, eav(1).copy(id = 1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testShiftNoteChord() {
    SMV()
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testShiftNoteChordPositionMoved() {
    SMV(73)
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 2), eav(1).copy(id = 1))
  }

  @Test
  fun testShiftNoteChordPositionMovedOctave() {
    SMV(61)
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2)
      )
    )
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 9), eav(1).copy(id = 1))
  }

  @Test
  fun testShiftNoteChordStemMoved() {
    SMV(60)
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 12,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, false, eav(1))
  }

  @Test
  fun testShiftNoteChordTwoSteps() {
    SMV()
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 2,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D, Accidental.NATURAL, 5, false),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testShiftNoteChordTwice() {
    SMV()
    repeat(2) {
      SAE(
        EventType.NOTE_SHIFT, eav(1), paramMapOf(
          EventParam.AMOUNT to 1,
          EventParam.ACCIDENTAL to Accidental.SHARP
        )
      )
    }
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D, Accidental.NATURAL, 5, false),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testShiftNoteChordTwiceVoiceWild() {
    SMV()
    repeat(2) {
      SAE(
        EventType.NOTE_SHIFT, ea(1), paramMapOf(
          EventParam.AMOUNT to 1,
          EventParam.ACCIDENTAL to Accidental.SHARP
        )
      )
    }
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D, Accidental.NATURAL, 5, false),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testShiftNoteInTwoNoteChord() {
    SMV()
    SMV(69)
    SAE(
      EventType.NOTE_SHIFT, eav(1).copy(id = 1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.A, Accidental.NATURAL, 4, false),
      eav(1).copy(id = 2)
    )
  }

  @Test
  fun testShiftNoteChordRange() {
    SMV()
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      ), eav(1, minim(1))
    )
    repeat(4) {
      SVP(
        EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, it == 0),
        eav(1, crotchet().multiply(it)).copy(id = 1)
      )
    }
  }


  @Test
  fun testShiftNoteChordRangeTied() {
    SMV(duration = breve())
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      ), eav(2)
    )
    repeat(2) { bar ->
      SVP(
        EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
        eav(bar+1).copy(id = 1)
      )
    }
  }

  @Test
  fun testShiftNoteOctaveRangeTied() {
    SMV(duration = breve())
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 12,
        EventParam.ACCIDENTAL to Accidental.SHARP
      ), eav(2)
    )
    repeat(2) { bar ->
      SVP(
        EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 6),
        eav(bar+1).copy(id = 1)
      )
    }
  }

  @Test
  fun testShiftNoteOctaveDownTied() {
    SMV(duration = breve())
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to -12,
        EventParam.ACCIDENTAL to Accidental.SHARP
      ), eav(2)
    )
    repeat(2) { bar ->
      SVP(
        EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 4),
        eav(bar+1).copy(id = 1)
      )
    }
  }


  @Test
  fun testShiftChordArticulationRetained() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    SVP(
      EventType.DURATION,
      EventParam.ARTICULATION,
      ChordDecoration(false, listOf(ArticulationType.ACCENT)),
      eav(1)
    )
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(
      EventType.DURATION,
      EventParam.ARTICULATION,
      ChordDecoration(false, listOf(ArticulationType.ACCENT)),
      eav(1)
    )
  }


  @Test
  fun testShiftNoteArticulationRetained() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    SVP(
      EventType.DURATION,
      EventParam.ARTICULATION,
      ChordDecoration(false, listOf(ArticulationType.ACCENT)),
      eav(1)
    )
    SAE(
      EventType.NOTE_SHIFT, eav(1).copy(id = 1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(
      EventType.DURATION,
      EventParam.ARTICULATION,
      ChordDecoration(false, listOf(ArticulationType.ACCENT)),
      eav(1)
    )
  }

  @Test
  fun testShiftNoteChordRangeVoiceNotSpecified() {
    SMV()
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    SAE(
      EventType.NOTE_SHIFT, ea(1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      ), ea(1, minim(1))
    )
    repeat(4) {
      SVP(
        EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, it == 0),
        eav(1, crotchet().multiply(it)).copy(id = 1)
      )
    }
  }

  @Test
  fun testShiftNoteRangeTuplet() {
    SAE(EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 3,
      EventParam.DENOMINATOR to 4))
    SMV()
    SMV(eventAddress = eav(1, Offset(1,6)))
    SAE(
      EventType.NOTE_SHIFT, eav(1).copy(id = 1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      ), eav(1, minim())
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, false),
      eav(1, Offset(1,6)).copy(id = 1)
    )
  }

  @Test
  fun testShiftNoteRangeTupletBeamDirection() {
    SAE(EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 3,
      EventParam.DENOMINATOR to 8))
    SMV(69, duration = quaver())
    SMV(69, duration = quaver(), eventAddress = eav(1, Offset(1,12)))
    SMV(69, duration = quaver(), eventAddress = eav(1, Offset(1,6)))
    SAE(
      EventType.NOTE_SHIFT, eav(1).copy(id = 1), paramMapOf(
        EventParam.AMOUNT to 2,
        EventParam.ACCIDENTAL to Accidental.SHARP
      ), eav(1, minim())
    )
    val beams = EG().getBeams()
    assertEqual(false, beams.toList().first().second.up)
  }

  @Test
  fun testShiftNoteRangeBeamDirection() {
    repeat(3)  {
      SMV(69, duration = quaver(), eventAddress =  eav(1, quaver() * it))
    }
    SAE(
      EventType.NOTE_SHIFT, eav(1).copy(id = 1), paramMapOf(
        EventParam.AMOUNT to 2,
        EventParam.ACCIDENTAL to Accidental.SHARP
      ), eav(1, minim())
    )
    val beams = EG().getBeams()
    assertEqual(false, beams.toList().first().second.up)
  }

  @Test
  fun testShiftNoteMovesTie() {
    SMV(duration = breve())
    SAE(
      EventType.NOTE_SHIFT, eav(1).copy(id = 1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eav(2).copy(id = 1)
    )
  }

  @Test
  fun testShiftChordMovesTie() {
    SMV(duration = breve())
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eav(2).copy(id = 1)
    )
  }

  @Test
  fun testShiftNoteMovesPitchTie() {
    SMV(71, duration = breve())
    SAE(
      EventType.NOTE_SHIFT, eav(1).copy(id = 1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5, false),
      eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5, false),
      eav(2).copy(id = 1)
    )
  }

  @Test
  fun testShiftNoteMovesPositionTie() {
    SMV(duration = breve())
    SAE(
      EventType.NOTE_SHIFT, eav(1).copy(id = 1), paramMapOf(
        EventParam.AMOUNT to -1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(
      EventType.NOTE, EventParam.POSITION, Coord(0,4),
      eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE, EventParam.POSITION, Coord(0,4),
      eav(2).copy(id = 1)
    )
  }

  @Test
  fun testShiftNoteMovesPitchTieForChord() {
    SMV(71, duration = breve())
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5, false),
      eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5, false),
      eav(2).copy(id = 1)
    )
  }

  @Test
  fun testShiftNoteOctaveAccidentalUnchanged() {
    SMV(73, accidental = Accidental.FLAT)
    SAE(
      EventType.NOTE_SHIFT, eav(1).copy(id = 1), paramMapOf(
        EventParam.AMOUNT to 12
      )
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D, Accidental.FLAT, 6, true),
      eav(1).copy(id = 1)
    )
  }


}