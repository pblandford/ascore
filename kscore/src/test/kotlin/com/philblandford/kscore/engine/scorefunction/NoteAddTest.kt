package com.philblandford.kscore.engine.scorefunction

import assertEqual

import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.dsl.scoreAllCrotchets
import com.philblandford.kscore.engine.dsl.scoreGrandStave
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.eventadder.util.getNote
import com.philblandford.kscore.engine.time.TimeSignature
import org.junit.Test

class NoteAddTest : ScoreTest() {

  @Test
  fun testAddNote() {
    SMV()
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteToChord() {
    SMV()
    SMV(69)
    val chord = getEvent(EventType.DURATION, eav(1))!!
    assertEqual(2, chord.getParam<Iterable<Event>>(EventParam.NOTES)?.toList()?.size)
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.A, Accidental.NATURAL, 4),
      eventAddress = eav(1).copy(id = 2)
    )
  }

  @Test
  fun testAddNoteDifferentDuration() {
    SMV()
    SMV(69, duration = minim())
    val chord = getEvent(EventType.DURATION, eav(1))!!
    assertEqual(1, chord.getParam<Iterable<Event>>(EventParam.NOTES)?.toList()?.size)
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.A, Accidental.NATURAL, 4),
      eventAddress = eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteVoice2() {
    SMV(eventAddress = eav(1, voice = 2))
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(1, voice = 2).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteVoice2StemDown() {
    SMV()
    SMV(eventAddress = eav(1, voice = 2), midiVal = 60)
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, false, eventAddress = eav(1, voice = 2))
  }

  @Test
  fun testAddNoteVoice1StemUp() {
    SMV()
    SMV(eventAddress = eav(1, voice = 2), midiVal = 60)
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, true, eventAddress = eav(1))
  }

  @Test
  fun testAddNoteVoice2RestCreated() {
    SMV()
    SMV(eventAddress = eav(1, voice = 2), midiVal = 60)
    SVP(
      EventType.DURATION,
      EventParam.TYPE,
      DurationType.REST,
      eventAddress = eav(1, voice = 2, offset = crotchet())
    )
    SVP(
      EventType.DURATION,
      EventParam.TYPE,
      DurationType.REST,
      eventAddress = eav(1, voice = 2, offset = minim())
    )
  }

  @Test
  fun testAddNoteVoice2LastCrotchet() {
    SMV(duration = quaver(), eventAddress = eav(1, minim(1), voice = 2))
    SVVM("R2:R4:C8:R8", eav(1, voice = 2))
  }

  @Test
  fun testTwoNotesVoice1() {
    SMV()
    SMV(eventAddress = eav(1, voice = 2), midiVal = 60)
    SMV(eventAddress = eav(1, crotchet()))
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, true, eventAddress = eav(1))
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, true, eventAddress = eav(1, crotchet()))
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, false, eventAddress = eav(1, voice = 2))
  }

  @Test
  fun testAddNoteAfterDotted6_8() {
    SAE(TimeSignature(6, 8).toEvent())
    SMV(duration = quaver(1))
    SMV(duration = semiquaver(), eventAddress = eav(1, quaver(1)))
    SMV(duration = quaver(), eventAddress = eav(1, crotchet()))
    SVVM("C3/16:C16:C8:R3/8", eav(1))
  }

  @Test
  fun testAddNotePositionSet() {
    SMV()
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 3), eventAddress = eav(1).copy(id = 1))
  }

  @Test
  fun testAddNoteStemDirectionCorrect() {
    SMV(midiVal = 60)
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, true, eventAddress = eav(1))
  }

  @Test
  fun testAddNoteStemDirectionCorrectDown() {
    SMV(midiVal = 72)
    SVB(EventType.DURATION, EventParam.IS_UPSTEM, false, eventAddress = eav(1))
  }

  @Test
  fun testAddNoteStave2() {
    sc.setNewScore(scoreGrandStave(1))
    SMV(eventAddress = eav(1).copy(staveId = StaveId(1, 2)))
    SVP(
      EventType.NOTE, EventParam.POSITION, Coord(0, 3),
      eventAddress = eav(1).copy(id = 1, staveId = StaveId(1, 2))
    )
  }

  @Test
  fun testAddNoteSharpShown() {
    SMV(73)
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eventAddress = eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteSharpNotShown() {
    SMV(72)
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5, false),
      eventAddress = eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteRange() {
    SMV(endAddress = eav(1, minim(1)))
    repeat(4) { offset ->
      SVP(EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eav(1, crotchet() * offset))
    }
  }

  @Test
  fun testAddNoteRangeOffset() {
    SAE(rest(minim()), eav(2))
    SAE(rest(crotchet()), eav(2, minim()))
    SMV(eventAddress = eav(1, minim()), endAddress = eav(2, minim()))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eav(1))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eav(1, minim()))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eav(2))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eav(2, minim()))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eav(2, minim(1)))
  }

  @Test
  fun testAddNoteRangePitchCorrect() {
    SMV(endAddress = eav(1, minim(1)))
    repeat(4) { offset ->
      SVP(
        EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 5),
        eav(1, crotchet().multiply(offset)).copy(id = 1)
      )
    }
  }

  @Test
  fun testAddNoteRangeIntoEmptyBar() {
    SMV(endAddress = eav(2))
    repeat(2) { bar ->
      repeat(4) { offset ->
        SVP(
          EventType.DURATION,
          EventParam.TYPE,
          DurationType.CHORD,
          eav(bar + 1, crotchet().multiply(offset))
        )
      }
    }
  }

  @Test
  fun testAddNoteAcrossBar() {
    SMV(duration = breve())
    SVP(EventType.NOTE, EventParam.DURATION, semibreve(), eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.DURATION, semibreve(), eav(2).copy(id = 1))
  }

  @Test
  fun testAddNoteAcrossLastBar() {
    SMV(duration = breve(), eventAddress = eav(EG().numBars))
    SVP(EventType.NOTE, EventParam.DURATION, semibreve(), eav(EG().numBars).copy(id = 1))
  }

  @Test
  fun testAddNoteAcrossLastBarTieNotCreated() {
    SMV(duration = breve(), eventAddress = eav(EG().numBars))
    SVNE(EventType.TIE, eav(EG().numBars).copy(id = 1))
  }

  @Test
  fun testDeleteChord() {
    SMV()
    SDE(EventType.DURATION, eav(1))
    SVNE(EventType.DURATION)
  }

  @Test
  fun testDeleteNote() {
    SMV()
    SDE(EventType.NOTE, eav(1).copy(id = 1))
    SVNE(EventType.DURATION)
  }

  @Test
  fun testDeleteNoteOneOfTwo() {
    SMV()
    SMV(74)
    SDE(EventType.NOTE, eav(1).copy(id = 1))
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testDeleteNoteTwoOfTwo() {
    SMV()
    SMV(74)
    SDE(EventType.NOTE, eav(1).copy(id = 2))
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.D, Accidental.NATURAL, 5),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testDeleteChordRange() {
    setNewScore(scoreAllCrotchets(4))
    SDE(EventType.DURATION, eav(1), endAddress = eav(1, crotchet()))
    SVVM("R2:C4:C4", eav(1))
  }

  @Test
  fun testDeleteNoteRangeAcrossBars() {
    setNewScore(scoreAllCrotchets(4))
    SDE(EventType.DURATION, eav(1), endAddress = eav(2))
    SVVM("", eav(1))
    SVVM("R4:C4:C4:C4", eav(2))
  }

  @Test
  fun testDeleteNoteRangeAcrossBarsStartOffset() {
    setNewScore(scoreAllCrotchets(4))
    SDE(EventType.DURATION, eav(1, minim()), endAddress = eav(2))
    SVVM("C4:C4:R2", eav(1))
    SVVM("R4:C4:C4:C4", eav(2))
  }

  @Test
  fun testAddNotesBeamed() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, quaver()))
  }

  @Test
  fun testAddNoteForceFlat() {
    SMV(accidental = Accidental.FORCE_SHARP)
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.B, Accidental.SHARP, 5, true),
      eventAddress = eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteForceFlatNormalNote() {
    SMV(69, accidental = Accidental.FORCE_SHARP)
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.A, Accidental.NATURAL, 4),
      eventAddress = eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteDoubleSharp() {
    SMV(accidental = Accidental.DOUBLE_SHARP, midiVal = 74)
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.DOUBLE_SHARP, 5, true),
      eventAddress = eav(1).copy(id = 1)
    )
  }


  @Test
  fun testAddNotePlusOctave() {
    SMV(extraParams = paramMapOf(EventParam.ADD_OCTAVE to true))
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 4),
      eventAddress = eav(1).copy(id = 2)
    )
  }

  @Test
  fun testAddNoteDottedRhythm() {
    SMV(extraParams = paramMapOf(EventParam.DOTTED_RHYTHM to true))
    SMV(
      extraParams = paramMapOf(EventParam.DOTTED_RHYTHM to true),
      eventAddress = eav(1, crotchet(1))
    )
    SVP(EventType.DURATION, EventParam.DURATION, crotchet(1), eventAddress = eav(1))
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eventAddress = eav(1, crotchet(1)))
  }

  @Test
  fun testAddNoteDottedRhythmThirdCrotchet() {
    SMV(extraParams = paramMapOf(EventParam.DOTTED_RHYTHM to true), eventAddress = eav(1, minim()))
    SMV(
      extraParams = paramMapOf(EventParam.DOTTED_RHYTHM to true),
      eventAddress = eav(1, Duration(7, 8))
    )
    SVP(EventType.DURATION, EventParam.DURATION, crotchet(1), eventAddress = eav(1, minim()))
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eventAddress = eav(1, Duration(7, 8)))
  }

  @Test
  fun testAddNoteDottedRhythmFourthCrotchet() {
    SMV(
      extraParams = paramMapOf(EventParam.DOTTED_RHYTHM to true),
      eventAddress = eav(1, minim(1)), duration = quaver()
    )
    SMV(
      extraParams = paramMapOf(EventParam.DOTTED_RHYTHM to true),
      eventAddress = eav(1, Duration(15, 16)), duration = quaver()
    )
    SVP(EventType.DURATION, EventParam.DURATION, quaver(1), eventAddress = eav(1, minim(1)))
    SVP(
      EventType.DURATION,
      EventParam.DURATION,
      semiquaver(),
      eventAddress = eav(1, Duration(15, 16))
    )
  }

  @Test
  fun testAddNoteTieToLast() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()), extraParams = paramMapOf(EventParam.TIE_TO_LAST to true))
    SVP(EventType.TIE, EventParam.DURATION, crotchet(), eventAddress = eav(1).copy(id = 1))
    SVP(EventType.TIE, EventParam.IS_END_TIE, true, eventAddress = eav(1, crotchet()).copy(id = 1))
  }

  @Test
  fun testAddNoteTieToLastAfterRest() {
    SAE(rest(crotchet()))
    SMV(eventAddress = eav(1, crotchet()), extraParams = paramMapOf(EventParam.TIE_TO_LAST to true))
    SVNE(EventType.TIE, eventAddress = eav(1, crotchet()).copy(id = 1))
    assert(EG().getNote(eav(1, crotchet()), midiVal = 72)?.second?.isEndTie == false)
  }

  @Test
  fun testAddNoteTieToLastDifferentNote() {
    SMV()
    SMV(
      70,
      eventAddress = eav(1, crotchet()),
      extraParams = paramMapOf(EventParam.TIE_TO_LAST to true)
    )
    SVP(
      EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eventAddress = eav(1)
    )
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eventAddress = eav(1, crotchet()))
    assert(EG().getNote(eav(1, crotchet()), midiVal = 70)?.second?.isEndTie == false)
  }

  @Test
  fun testAddNoteTieToLastPlusOctave() {
    SMV(extraParams = paramMapOf(EventParam.ADD_OCTAVE to true))
    SMV(
      eventAddress = eav(1, crotchet()), extraParams = paramMapOf(
        EventParam.TIE_TO_LAST to true,
        EventParam.ADD_OCTAVE to true
      )
    )
    SVP(
      EventType.TIE, EventParam.DURATION, crotchet(), eventAddress = eav(1).copy(id = 1)
    )
    SVP(
      EventType.TIE, EventParam.DURATION, crotchet(), eventAddress = eav(1).copy(id = 2)
    )
    SVP(EventType.TIE, EventParam.IS_END_TIE, true, eventAddress = eav(1, crotchet()).copy(id = 1))
    SVP(EventType.TIE, EventParam.IS_END_TIE, true, eventAddress = eav(1, crotchet()).copy(id = 2))
  }

  @Test
  fun testAddNoteTieToLastV2() {
    SMV(eventAddress = eav(1, dZero(), 2))
    SMV(
      eventAddress = eav(1, crotchet(), 2),
      extraParams = paramMapOf(EventParam.TIE_TO_LAST to true)
    )
    SVP(
      EventType.TIE,
      EventParam.DURATION,
      crotchet(),
      eventAddress = eav(1, voice = 2).copy(id = 1)
    )
    SVP(
      EventType.TIE,
      EventParam.IS_END_TIE,
      true,
      eventAddress = eav(1, crotchet(), 2).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteTieToLastV2_V1ChordIsCloser() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    SMV(eventAddress = eav(1, dZero(), 2))
    SMV(
      eventAddress = eav(1, crotchet(), 2),
      extraParams = paramMapOf(EventParam.TIE_TO_LAST to true)
    )
    SVP(
      EventType.TIE,
      EventParam.DURATION,
      crotchet(),
      eventAddress = eav(1, voice = 2).copy(id = 1)
    )
    SVP(
      EventType.TIE,
      EventParam.IS_END_TIE,
      true,
      eventAddress = eav(1, crotchet(), 2).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteTieToLastOnlyNewNoteIsTied() {
    SMV()
    SMV(69)
    SMV(eventAddress = eav(1, crotchet()), extraParams = paramMapOf(EventParam.TIE_TO_LAST to true))
    SVP(EventType.NOTE, EventParam.IS_START_TIE, true, eventAddress = eav(1).copy(id = 1))
    SVNP(EventType.TIE, EventParam.IS_START_TIE, eventAddress = eav(1).copy(id = 2))
  }

  @Test
  fun testAddNoteReplaceEndTie() {
    SMV(duration = breve())
    SMV(duration = semibreve(), eventAddress = eav(2))
    assertEqual(
      1,
      (EG().getParam(EventType.DURATION, EventParam.NOTES, eav(2)) as? List<Event>)?.size
    )
  }

  @Test
  fun testAddNoteReplaceEndTieAccidental() {
    SMV(73, duration = breve())
    SMV(73, duration = semibreve(), eventAddress = eav(2))
    assertEqual(
      1,
      (EG().getParam(EventType.DURATION, EventParam.NOTES, eav(2)) as? List<Event>)?.size
    )
  }

  @Test
  fun testDeleteClosesRest() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SMV(eventAddress = eav(1, minim()))
    SDE(EventType.DURATION, eav(1))
    SDE(EventType.DURATION, eav(1, crotchet()))
    SVVM("R2:C4:R4", eav(1))
  }

  @Test
  fun testDeleteClosesRestSecondMinim() {
    SMV()
    SMV(eventAddress = eav(1, minim()))
    SDE(EventType.DURATION, eav(1, minim()))
    SVVM("C4:R4:R2", eav(1))
  }

  @Test
  fun testDeleteClosesRestLastCrotchet() {
    SMV()
    SMV(eventAddress = eav(1, minim()))
    SMV(eventAddress = eav(1, minim(1)))
    SDE(EventType.DURATION, eav(1, minim()))
    SDE(EventType.DURATION, eav(1, minim(1)))
    SVVM("C4:R4:R2", eav(1))
  }


  @Test
  fun testDeleteLeavesPreviousCrotchet() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SMV(eventAddress = eav(1, minim()))
    SDE(EventType.DURATION, eav(1, crotchet()))
    SVVM("C4:R4:C4:R4", eav(1))
  }

  @Test
  fun testAddNotePastBarLine() {
    SMV(duration = breve())
    SVP(EventType.DURATION, EventParam.DURATION, semibreve(), eventAddress = eav(2))
  }

  @Test
  fun testAddNotePastBarLineTiesAdded() {
    SMV(duration = breve())
    SVP(EventType.TIE, EventParam.DURATION, semibreve(), eventAddress = eav(1).copy(id = 1))
    SVP(EventType.TIE, EventParam.IS_END_TIE, true, eventAddress = eav(2).copy(id = 1))
  }

  @Test
  fun testAddNoteOverTupletSplit() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(duration = crotchet(), eventAddress = eav(1, Duration(1, 6)))
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1, Duration(1, 6)))
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1, crotchet()))
  }

  @Test
  fun testAddNoteNotMarkedTie() {
    SMV()
    SVP(EventType.NOTE, EventParam.IS_START_TIE, false, eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.IS_END_TIE, false, eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.END_TIE, dZero(), eav(1).copy(id = 1))
  }

  @Test
  fun testAddCrotchetAtDottedQuaverOffset() {
    SMV(duration = semiquaver())
    SMV(duration = quaver(), eventAddress = eav(1, semiquaver()))
    SMV(duration = crotchet(), eventAddress = eav(1, quaver(1)))
    SVP(EventType.NOTE, EventParam.DURATION, crotchet(), eav(1, quaver(1)).copy(id = 1))
  }

  @Test
  fun testAddNotePercussion() {
    SCD(instruments = listOf("Bass Drum 1"))
    SMV(35)
    SVP(EventType.NOTE, EventParam.PERCUSSION, true, eventAddress = eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.MIDIVAL, 35, eventAddress = eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 4), eventAddress = eav(1).copy(id = 1))
  }

  @Test
  fun testAddNotePercussionCrossHead() {
    SCD(instruments = listOf("Open Hi-hat"))
    SMV(46)
    SVP(
      EventType.NOTE,
      EventParam.NOTE_HEAD_TYPE,
      NoteHeadType.CROSS,
      eventAddress = eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddNotePercussionDifferentDuration() {
    SCD(instruments = listOf("Kit"))
    SMV(48)
    SMV(43, quaver())
    SVP(EventType.NOTE, EventParam.MIDIVAL, 48, eventAddress = eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.MIDIVAL, 43, eventAddress = eav(1).copy(id = 2))
    SVP(EventType.NOTE, EventParam.DURATION, quaver(), eventAddress = eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.DURATION, quaver(), eventAddress = eav(1).copy(id = 2))
  }

  @Test
  fun testDeleteRest() {
    SMV(eventAddress = eav(1, voice = 2))
    SDE(EventType.DURATION, eav(1, crotchet(), 2))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.EMPTY, eav(1, crotchet(), 2))
  }

  @Test
  fun testAddNoteRestGone() {
    SMV()
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eav(1))
    SVNE(EventType.DURATION, eav(1, voice = 2))
  }

  @Test
  fun testAddNoteUpbeatBar() {
    SAE(TimeSignature(1, 4, hidden = true).toEvent(), ez(1))
    SMV()
    SVVM("C4", eav(1))
  }

  @Test
  fun testAddNoteUpbeatBarVoice2() {
    SAE(TimeSignature(1, 4, hidden = true).toEvent(), ez(1))
    SMV(eventAddress = eav(1, dZero(), 2))
    SVVM("C4", eav(1, dZero(), 2))
  }

  @Test
  fun testAddNotePercussionDownStem() {
    SCD(instruments = listOf("Kit"))
    SMV(35)
    SVP(EventType.NOTE, EventParam.PERCUSSION, true, eventAddress = eav(1, voice = 2).copy(id = 1))
    SVP(EventType.NOTE, EventParam.MIDIVAL, 35, eventAddress = eav(1, voice = 2).copy(id = 1))
    SVP(
      EventType.NOTE,
      EventParam.POSITION,
      Coord(0, 7),
      eventAddress = eav(1, voice = 2).copy(id = 1)
    )
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, false, eventAddress = eav(1, voice = 2))
  }

  @Test
  fun testAddNotePercussionPositionCorrect() {
    SCD(instruments = listOf("Kit"))
    SMV(37)
    SMV(48)
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 1), eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 3), eav(1).copy(id = 2))
  }

  @Test
  fun testTemp() {
    SMV(midiVal = 76)
    SMV(midiVal = 72)
  }

  @Test
  fun testAddNoteTranspose() {
    SCD(instruments = listOf("Trumpet"))
    SMV(60)
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 4, true),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteTransposeTenorSax() {
    SCD(instruments = listOf("Tenor Sax"))
    SMV(60)
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 4, true),
      eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE, EventParam.POSITION, Coord(0, 10), eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteNoDuplicate() {
    SMV()
    SMV()
    assertEqual(
      1,
      EG().getParam<Iterable<Event>>(EventType.DURATION, EventParam.NOTES, eav(1))?.count()
    )
  }

  @Test
  fun testAddNoteNoDuplicateDifferentParam() {
    SMV()
    SMV(extraParams = paramMapOf(EventParam.IS_SMALL to true))
    assertEqual(
      1,
      EG().getParam<Iterable<Event>>(EventType.DURATION, EventParam.NOTES, eav(1))?.count()
    )
  }

  @Test
  fun testAddNoteDiamond() {
    SMV(extraParams = paramMapOf(EventParam.NOTE_HEAD_TYPE to NoteHeadType.DIAMOND))
    SVP(
      EventType.NOTE,
      EventParam.NOTE_HEAD_TYPE,
      NoteHeadType.DIAMOND,
      eventAddress = eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteSameYPosition() {
    SMV(72)
    SMV(73)
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eventAddress = eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5, true),
      eventAddress = eav(1).copy(id = 2)
    )
  }


  @Test
  fun testAddNoteHoldMarkerRemains() {
    SMV(extraParams = paramMapOf(EventParam.HOLD to true))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), eZero())
  }

  @Test
  fun testAddNoteWildcard() {
    SMV(eventAddress = eWild())
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteWildcardBar2() {
    setMarker(ea(2))
    SMV(eventAddress = eWild())
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(2).copy(id = 1)
    )
  }


  @Test
  fun testAddNoteWildcardVoice2() {
    SMV(eventAddress = eWild().copy(voice = 2))
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(1, voice = 2).copy(id = 1)
    )
  }

}