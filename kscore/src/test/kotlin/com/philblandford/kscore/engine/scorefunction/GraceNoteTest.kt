package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.duration.*
import grace
import graceRest
import org.junit.Test

class GraceNoteTest : ScoreTest() {

  @Test
  fun testAddNoteGrace() {
    SMV()
    grace()
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(1).copy(id = 1, graceOffset = dZero())
    )
  }

  @Test
  fun testAddNoteGracePositionCorrect() {
    SMV()
    grace()
    SVP(
      EventType.NOTE, EventParam.POSITION, Coord(0, 3),
      eventAddress = eav(1).copy(id = 1, graceOffset = dZero())
    )
  }

  @Test
  fun testAddNoteGraceSmall() {
    SMV()
    grace()
    SVP(
      EventType.NOTE, EventParam.IS_SMALL, true,
      eventAddress = eav(1).copy(id = 1, graceOffset = dZero())
    )
  }

  @Test
  fun testAcciaccaturaHasSlash() {
    SMV()
    grace(type = GraceType.ACCIACCATURA)
    SVP(
      EventType.DURATION, EventParam.IS_SLASH, true,
      eventAddress = eav(1).copy(graceOffset = dZero())
    )
  }

  @Test
  fun testAddNoteGraceShift() {
    SMV()
    grace()
    grace()
    SVE(EventType.DURATION, eav(1).copy(graceOffset = dZero()))
    SVE(EventType.DURATION, eav(1).copy(graceOffset = semiquaver()))
  }

  @Test
  fun testAddNoteGraceShiftNotAddedToChord() {
    SMV()
    grace()
    grace()
    SVNE(EventType.NOTE, eav(1).copy(graceOffset = dZero(), id = 2))
  }

  @Test
  fun testAddNoteGraceToChord() {
    SMV()
    grace()
    grace(mode = GraceInputMode.ADD, midiVal = 60)
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(1).copy(id = 1, graceOffset = dZero())
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 4),
      eventAddress = eav(1).copy(id = 2, graceOffset = dZero())
    )
  }

  @Test
  fun testAddNoteGraceToChordMarkerMovesToNextGrace() {
    SMV()
    grace()
    grace()
    grace(mode = GraceInputMode.ADD, midiVal = 60)
    assertEqual(eag(1, graceOffset = semiquaver()), getMarker())
  }

  @Test
  fun testAddNoteGraceToChordMarkerMovesToMainOffset() {
    SMV()
    grace()
    grace(mode = GraceInputMode.ADD, midiVal = 60)
    assertEqual(ea(1), getMarker())
  }

  @Test
  fun testAddNoteGraceNoGraceOffset() {
    SMV()
    grace(null)
    SVE(EventType.DURATION, eav(1))
    SVE(EventType.DURATION, eav(1).copy(graceOffset = dZero()))
  }

  @Test
  fun testAddNoteGraceInfoNotSaved() {
    SMV()
    grace()
    grace()
    SVNP(EventType.DURATION, EventParam.GRACE_MODE, eav(1).copy(graceOffset = dZero()))
    SVNP(EventType.DURATION, EventParam.GRACE_TYPE, eav(1).copy(graceOffset = semiquaver()))
  }

  @Test
  fun testAddNoteGraceEmptyBarRestCreated() {
    grace()
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(1).copy(id = 1, graceOffset = dZero())
    )
    SVP(EventType.DURATION, EventParam.DURATION, semibreve(), eav(1))
  }

  @Test
  fun testAddNoteGraceShiftMarkerCorrect() {
    grace()
    SVP(
      EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), eZero()
    )
  }

  @Test
  fun testAddNoteGraceShiftBeforeNoteMarkerCorrect() {
    grace()
    grace()
    SVP(
      EventType.UISTATE, EventParam.MARKER_POSITION, eag(1, graceOffset = semiquaver()), eZero()
    )
  }

  @Test
  fun testAddNoteGraceAddMarkerCorrect() {
    grace(mode = GraceInputMode.ADD)
    SVP(
      EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), eZero()
    )
  }

  @Test
  fun testAddGraceNotGraceOffsetAddMarkerMoves() {
    SMV()
    SMV(
      extraParams = paramMapOf(
        EventParam.GRACE_TYPE to GraceType.APPOGGIATURA,
        EventParam.GRACE_MODE to GraceInputMode.ADD
      )
    )
    assertEqual(ea(1), getMarker())

  }

  @Test
  fun testAddNoteGraceNonGraceOffsetMarkerMoves() {
    grace(null)
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, dZero()), eZero())
  }

  @Test
  fun testAddNoteGraceNonGraceOffsetNoteAppended() {
    grace(null, midiVal = 60)
    grace(null, midiVal = 62)
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C), eagv(1).copy(id = 1))
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.D),
      eagv(1, dZero(), semiquaver()).copy(id = 1)
    )
  }

  @Test
  fun testAddNoteBeamsCreated() {
    SMV()
    grace()
    grace()
    val beams = EG().getBeams()
    assertEqual(1, beams.size)
    val beam = beams.toList().first()
    assertEqual(eagv(), beam.first)
    assertEqual(2, beam.second.members.count())
  }

  @Test
  fun testAddNoteMarkedBeamed() {
    SMV()
    grace()
    grace()
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eagv(1))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eagv(1, dZero(), semiquaver()))
  }

  @Test
  fun testSingleNoteBeamsNotCreated() {
    SMV()
    grace()
    val beams = EG().getBeams()
    assertEqual(0, beams.size)
  }

  @Test
  fun testAddNoteGraceReplaceRestWithChord() {
    grace()
    SMV()
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(1).copy(id = 1, graceOffset = dZero())
    )
  }

  @Test
  fun testDeleteNoteGrace() {
    SMV()
    grace()
    SDE(EventType.DURATION, eagv(1))
    SVNE(EventType.DURATION, eagv(1))
  }

  @Test
  fun testDeleteNoteGraceOthersShifted() {
    SMV()
    grace()
    grace()
    SDE(EventType.DURATION, eagv(1))
    SVE(EventType.DURATION, eagv(1))
    SVNE(EventType.DURATION, eagv(1, dZero(), semiquaver()))
  }

  @Test
  fun testDeleteNoteGraceMarkerPositioned() {
    SMV()
    grace()
    SDE(EventType.DURATION, eagv(1))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), eZero())
  }

  @Test
  fun testAddNoteGraceStemDirection() {
    SMV()
    grace()
    SVP(
      EventType.DURATION, EventParam.IS_UPSTEM, false,
      eventAddress = eav(1).copy(graceOffset = dZero())
    )
  }

  @Test
  fun testAddNoteGraceStemDirectionVoice1() {
    SMV()
    grace()
    grace(voice = 2)
    SVP(
      EventType.DURATION, EventParam.IS_UPSTEM, true,
      eventAddress = eav(1).copy(graceOffset = dZero())
    )
    SVP(
      EventType.DURATION, EventParam.IS_UPSTEM, false,
      eventAddress = eagv(1, voice = 2).copy(graceOffset = dZero())
    )
  }

  @Test
  fun testDeleteMainNoteGraceRemains() {
    SMV()
    grace()
    SDE(EventType.DURATION, eav(1))
    SVE(EventType.DURATION, eagv(1))
  }

  @Test
  fun testAddNoteGraceAddOctave() {
    SMV()
    grace(params = paramMapOf(EventParam.ADD_OCTAVE to true))
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eventAddress = eav(1).copy(id = 1, graceOffset = dZero())
    )
    SVP(
      EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.NATURAL, 4),
      eventAddress = eav(1).copy(id = 2, graceOffset = dZero())
    )
    SVNE(EventType.NOTE, eav(1).copy(id = 1, graceOffset = semiquaver()))
  }

  @Test
  fun testAddNoteGraceDottedRhythm() {
    SMV()
    grace(offset = dZero(), params = paramMapOf(EventParam.DOTTED_RHYTHM to true))
    grace(offset = semiquaver(1), params = paramMapOf(EventParam.DOTTED_RHYTHM to true))
    SVP(EventType.DURATION, EventParam.DURATION, semiquaver(1), eagv())
    SVP(
      EventType.DURATION,
      EventParam.DURATION,
      demisemiquaver(),
      eagv(graceOffset = semiquaver(1))
    )
  }

  @Test
  fun testAddNoteGraceDottedRhythmAddMode() {
    SMV()
    grace(
      offset = dZero(), params = paramMapOf(EventParam.DOTTED_RHYTHM to true),
      mode = GraceInputMode.ADD
    )
    grace(
      offset = semiquaver(1), params = paramMapOf(EventParam.DOTTED_RHYTHM to true),
      mode = GraceInputMode.ADD
    )
    SVP(EventType.DURATION, EventParam.DURATION, semiquaver(1), eagv())
    SVP(
      EventType.DURATION,
      EventParam.DURATION,
      demisemiquaver(),
      eagv(graceOffset = semiquaver(1))
    )
  }

  @Test
  fun testAddNoteGraceDottedRhythmAddModeSecondBeat() {
    SMV()
    grace(
      offset = dZero(), params = paramMapOf(EventParam.DOTTED_RHYTHM to true),
      mainOffset = crotchet(),
      mode = GraceInputMode.ADD
    )
    grace(
      offset = semiquaver(1), params = paramMapOf(EventParam.DOTTED_RHYTHM to true),
      mainOffset = crotchet(),
      mode = GraceInputMode.ADD
    )
    SVP(EventType.DURATION, EventParam.DURATION, semiquaver(1), eagv(offset = crotchet()))
    SVP(
      EventType.DURATION,
      EventParam.DURATION,
      demisemiquaver(),
      eagv(offset = crotchet(), graceOffset = semiquaver(1))
    )
  }


  @Test
  fun testAddNoteGraceDottedRhythmNoGraceOffset() {
    SMV()
    grace(offset = null, params = paramMapOf(EventParam.DOTTED_RHYTHM to true))
    grace(offset = null, params = paramMapOf(EventParam.DOTTED_RHYTHM to true))
    SVP(EventType.DURATION, EventParam.DURATION, semiquaver(1), eagv())
    SVP(
      EventType.DURATION,
      EventParam.DURATION,
      demisemiquaver(),
      eagv(graceOffset = semiquaver(1))
    )
  }

  @Test
  fun testAddRestGrace() {
    SMV()
    graceRest()
    SVP(EventType.DURATION, EventParam.DURATION, semiquaver(), eagv())
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eagv())
  }

  @Test
  fun testAddRestGraceNullOffset() {
    SMV()
    graceRest(null)
    SVP(EventType.DURATION, EventParam.DURATION, semiquaver(), eagv())
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eagv())
  }
}