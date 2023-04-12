package pitch

import com.philblandford.kscore.engine.types.Accidental.*
import com.philblandford.kscore.engine.types.NoteLetter
import com.philblandford.kscore.engine.types.NoteLetter.*
import com.philblandford.kscore.engine.types.Pitch
import assertEqual
import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.engine.pitch.*
import org.junit.Test

class PitchTest {
  @Test
  fun testMidiVal() {
    assertEqual(60, Pitch(C, NATURAL, 4).midiVal)
  }

  @Test
  fun testMidiValUpOctave() {
    assertEqual(72, Pitch(C, NATURAL, 5).midiVal)

  }

  @Test
  fun testMidiValDownOctave() {
    assertEqual(48, Pitch(C, NATURAL, 3).midiVal)

  }

  @Test
  fun testMidiValSharp() {
    assertEqual(61, Pitch(C, SHARP, 4).midiVal)
  }

  @Test
  fun testMidiValFlat() {
    assertEqual(61, Pitch(D, FLAT, 4).midiVal)
  }

  @Test
  fun testMidiValDoubleSharp() {
    assertEqual(62, Pitch(C, DOUBLE_SHARP, 4).midiVal)
  }

  @Test
  fun testMidiValDoubleFlat() {
    assertEqual(60, Pitch(D, DOUBLE_FLAT, 4).midiVal)
  }

  @Test
  fun testMidiValBSharp() {
    assertEqual(60, Pitch(B, SHARP, 4).midiVal)
  }

  @Test
  fun testMidiValCFlat() {
    assertEqual(59, Pitch(C, FLAT, 3).midiVal)
  }

  @Test
  fun testMidiValESharp() {
    assertEqual(65, Pitch(E, SHARP, 4).midiVal)
  }

  @Test
  fun testMidiValFFlat() {
    assertEqual(64, Pitch(F, FLAT, 4).midiVal)
  }


  @Test
  fun testGetNoteShift() {
    val pitch = Pitch(C, NATURAL, 5).getNoteShift(2)
    assertEqual(Pitch(NoteLetter.D, NATURAL, 5), pitch)
  }

  @Test
  fun testGetNoteDown() {
    val pitch = Pitch(NoteLetter.D, NATURAL, 5).getNoteShift(-2)
    assertEqual(Pitch(C, NATURAL, 5), pitch)
  }

  @Test
  fun testGetNoteDownCrossOctave() {
    val pitch = Pitch(C, NATURAL, 5).getNoteShift(-1)
    assertEqual(Pitch(B, NATURAL, 4), pitch)
  }

  @Test
  fun testGetNoteUpSharp() {
    val pitch = Pitch(C, NATURAL, 5).getNoteShift(1)
    assertEqual(Pitch(C, SHARP, 5), pitch)
  }

  @Test
  fun testGetPitchFromMidiValNaturalSimpleCase() {
    assertEqual(Pitch(C, NATURAL, 4), getPitchFromMidiVal(60, NATURAL))
  }

  @Test
  fun testGetPitchFromMidiValSharp() {
    assertEqual(Pitch(C, SHARP, 4), getPitchFromMidiVal(61, SHARP))
  }

  @Test
  fun testGetPitchFromMidiValSharpPreferredNatural() {
    assertEqual(Pitch(C, SHARP, 4), getPitchFromMidiVal(61, NATURAL))
  }

  @Test
  fun testGetPitchFromMidiValBSharp() {
    assertEqual(Pitch(B, SHARP, 4), getPitchFromMidiVal(60, FORCE_SHARP))
  }

  @Test
  fun testGetPitchFromMidiValForceSharpBNatural() {
    assertEqual(Pitch(B, NATURAL, 3), getPitchFromMidiVal(59, FORCE_SHARP))
  }

  @Test
  fun testGetPitchFromMidiValCNatural() {
    assertEqual(Pitch(C, NATURAL, 4), getPitchFromMidiVal(60, NATURAL))
  }

  @Test
  fun testGetPitchFromMidiValBFlat() {
    assertEqual(Pitch(B, FLAT, 4), getPitchFromMidiVal(70, FLAT))
  }

  @Test
  fun testGetPitchFromMidiValForceBFlat() {
    assertEqual(Pitch(B, FLAT, 4), getPitchFromMidiVal(70, FORCE_FLAT))
  }

  @Test
  fun testGetPitchFromMidiValFInSharpMode() {
    assertEqual(Pitch(F, NATURAL, 4), getPitchFromMidiVal(65, SHARP))
  }

  @Test
  fun testGetPitchFromMidiValESharp() {
    assertEqual(Pitch(NoteLetter.E, SHARP, 4), getPitchFromMidiVal(65, FORCE_SHARP))
  }

  @Test
  fun testGetPitchFromMidiValCFlat() {
    assertEqual(Pitch(C, FLAT, 3), getPitchFromMidiVal(59, FORCE_FLAT))
  }

  @Test
  fun testGetPitchFromMidiValForceSharp() {
    assertEqual(Pitch(F, SHARP, 4), getPitchFromMidiVal(66, FORCE_SHARP))
  }

  @Test
  fun testGetPitchFromMidiValFDoubleSharp() {
    assertEqual(Pitch(F, DOUBLE_SHARP, 4), getPitchFromMidiVal(67, DOUBLE_SHARP))
  }

  @Test
  fun testGetPitchFromMidiValNoEDoubleSharp() {
    assertEqual(Pitch(F, SHARP, 4), getPitchFromMidiVal(66, DOUBLE_SHARP))
  }

  @Test
  fun testGetPitchFromMidiValGDoubleFlat() {
    assertEqual(Pitch(G, DOUBLE_FLAT, 4), getPitchFromMidiVal(65, DOUBLE_FLAT))
  }

  @Test
  fun testGetPitchFromMidiValBDoubleFlat() {
    assertEqual(Pitch(B, DOUBLE_FLAT, 4), getPitchFromMidiVal(69, DOUBLE_FLAT))
  }

  @Test
  fun testGetPitchFromMidiValNoFDoubleFlat() {
    assertEqual(Pitch(NoteLetter.E, FLAT, 4), getPitchFromMidiVal(63, DOUBLE_FLAT))
  }

  @Test
  fun testGetPitchFromMidiValDoubleFlatOnBlackNote() {
    assertEqual(Pitch(NoteLetter.D, FLAT, 4), getPitchFromMidiVal(61, DOUBLE_FLAT))
  }

  @Test
  fun testGetPitchFromMidiValDoubleSharpOnBlackNote() {
    assertEqual(Pitch(NoteLetter.D, SHARP, 4), getPitchFromMidiVal(63, DOUBLE_SHARP))
  }

  @Test
  fun testTransposeNote() {
    val newNote = Pitch(C, SHARP, 5).transposeNote(2, SHARP)
    assertEqual(Pitch(NoteLetter.D, SHARP, 5), newNote)
  }

  @Test
  fun testPitchAtStep() {
    val newNote = Pitch(C, NATURAL, 5).pitchAtStep( 1, 0)
    assertEqual(Pitch(NoteLetter.D, NATURAL, 5), newNote)
  }

  @Test
  fun testPitchAtStepDown() {
    val newNote = Pitch(NoteLetter.E, NATURAL, 5).pitchAtStep( -1, 0)
    assertEqual(Pitch(NoteLetter.D, NATURAL, 5), newNote)
  }

  @Test
  fun testPitchAtStepSemitoneUp() {
    val newNote = Pitch(NoteLetter.E, NATURAL, 5).pitchAtStep( 1, 0)
    assertEqual(Pitch(F, NATURAL, 5), newNote)
  }

  @Test
  fun testPitchAtStepOctaveBoundary() {
    val newNote = Pitch(B, NATURAL, 5).pitchAtStep( 1, 0)
    assertEqual(Pitch(C, NATURAL, 6), newNote)
  }

  @Test
  fun testPitchAtStepDownOctaveBoundary() {
    val newNote = Pitch(C, NATURAL, 5).pitchAtStep(-1, 0)
    assertEqual(Pitch(B, NATURAL, 4), newNote)
  }

  @Test
  fun testPitchesInScale() {
    val pitches = Pitch(C, NATURAL, 4).pitchesInScale(0)
    assertEqual(
      listOf(
        Pitch(C, NATURAL, 4),
        Pitch(D, NATURAL, 4),
        Pitch(E, NATURAL, 4),
        Pitch(F, NATURAL, 4),
        Pitch(G, NATURAL, 4),
        Pitch(A, NATURAL, 4),
        Pitch(B, NATURAL, 4)
      ).toList(), pitches.toList()
    )
  }

  @Test
  fun testPitchesInScaleSharp() {
    val pitches = Pitch(C, SHARP, 4).pitchesInScale( 7)
    assertEqual(
      listOf(
        Pitch(C, SHARP, 4),
        Pitch(D, SHARP, 4),
        Pitch(E, SHARP, 4),
        Pitch(F, SHARP, 4),
        Pitch(G, SHARP, 4),
        Pitch(A, SHARP, 4),
        Pitch(B, SHARP, 4)
      ).toList(), pitches.toList()
    )
  }

  @Test
  fun testPitchesInScaleFlat() {
    val pitches = Pitch(C, FLAT, 4).pitchesInScale(-7)
    assertEqual(
      listOf(
        Pitch(C, FLAT, 4),
        Pitch(D, FLAT, 4),
        Pitch(E, FLAT, 4),
        Pitch(F, FLAT, 4),
        Pitch(G, FLAT, 4),
        Pitch(A, FLAT, 4),
        Pitch(B, FLAT, 4)
      ).toList(), pitches.toList()
    )
  }

  @Test
  fun testPositionToPitch() {
    val pitch = positionToPitch(0, ClefType.TREBLE)
    assertEqual(Pitch(F, NATURAL, 5), pitch)
  }

  @Test
  fun testPositionToPitchBelow() {
    val pitch = positionToPitch(4, ClefType.TREBLE)
    assertEqual(Pitch(B, NATURAL, 4), pitch)
  }

  @Test
  fun testPositionToPitchAbove() {
    val pitch = positionToPitch(-2, ClefType.TREBLE)
    assertEqual(Pitch(A, NATURAL, 5), pitch)
  }

  @Test
  fun testPitchToPosition() {
    val position = pitchToPosition(Pitch(F, NATURAL, 5), ClefType.TREBLE)
    assertEqual(0, position)
  }

  @Test
  fun testPitchToPositionAbove() {
    val position = pitchToPosition(Pitch(A, NATURAL, 5), ClefType.TREBLE)
    assertEqual(-2, position)
  }

  @Test
  fun testPitchToPositionBelow() {
    val position = pitchToPosition(Pitch(B, NATURAL, 4), ClefType.TREBLE)
    assertEqual(4, position)
  }

  @Test
  fun testEnharmonic() {
    val enharmonic = Pitch(E, FLAT, 4).enharmonic(SHARP)
    assertEqual(Pitch(D, SHARP, 4), enharmonic)
  }

  @Test
  fun testGetScale() {
    val scale = Pitch(E, FLAT, 4).getScale()
    assertEqual(
      listOf(
        Pitch(E, FLAT), Pitch(F), Pitch(G), Pitch(A, FLAT), Pitch(B, FLAT), Pitch(C, octave = 5), Pitch(D, octave = 5)
      ).toList(), scale.toList()
    )
  }

  @Test
  fun testGetScaleCFlatCorrectOctave() {
    val scale = Pitch(G, FLAT, 4).getScale()
    assertEqual(
      listOf(
        Pitch(G, FLAT), Pitch(A, FLAT), Pitch(B, FLAT), Pitch(C, FLAT), Pitch(D, FLAT, 5), Pitch(E, FLAT, 5), Pitch(F, octave = 5)
      ).toList(), scale.toList()
    )
  }

  @Test
  fun testGetScaleBSharpCorrectOctave() {
    val scale = Pitch(C, SHARP, 4).getScale()
    assertEqual(
      listOf(
        Pitch(C, SHARP), Pitch(D, SHARP), Pitch(E, SHARP), Pitch(F, SHARP), Pitch(G, SHARP), Pitch(A, SHARP), Pitch(B, SHARP, 5)
      ).toList(), scale.toList()
    )
  }
}