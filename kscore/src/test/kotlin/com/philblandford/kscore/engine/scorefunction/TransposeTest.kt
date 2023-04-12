package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.pitch.Harmony
import org.junit.Test

class TransposeTest : ScoreTest() {

  @Test
  fun testTransposeByAll() {
    SMV()
    transposeBy(2)
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 2, ez(1))
  }

  @Test
  fun testTransposeByAllPitchChanged() {
    SMV()
    transposeBy(2)
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.D, Accidental.NATURAL, 5),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testTransposeByAllFlatToSharpKey() {
    SCD(ks = -6)
    SMV(66, accidental = Accidental.FLAT)
    transposeBy(-2)
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.E, Accidental.NATURAL, 4),
      eav(1).copy(id = 1)
    )
  }


  @Test
  fun testTransposeByAllPositionChanged() {
    SMV()
    transposeBy(2)
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 2), eav(1).copy(id = 1))
  }

  @Test
  fun testTransposeByAllPositionChangedWithOctave() {
    SMV()
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ea(2)))
    transposeBy(2)
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 2), eav(1).copy(id = 1))
  }

  @Test
  fun testTransposeByAllStemChanged() {
    SMV(71)
    transposeBy(2)
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, false, eav(1))
  }

  @Test
  fun testTransposeByAllClefMidBar() {
    SMV(60)
    SMV(60, eventAddress = eav(1, minim()))
    SAE(EventType.CLEF, ea(1, minim()), paramMapOf(EventParam.TYPE to ClefType.ALTO))
    transposeBy(2)
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 9), eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 3), eav(1, minim()).copy(id = 1))
  }

  @Test
  fun testTransposeByAllStemChangedBeam() {
    SMV(69, quaver())
    SMV(71, quaver(), eventAddress = eav(1, quaver()))
    transposeBy(2)
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, false, eav(1))
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, false, eav(1, quaver()))
  }

  @Test
  fun testTransposeByAllBeamDirectionChanged() {
    SMV(69, quaver())
    SMV(71, quaver(), eventAddress = eav(1, quaver()))
    transposeBy(2)
    val beam = EG().getBeams().toList().first().second
    assert(!beam.up)
  }

  @Test
  fun testTransposeByAllPositionAccidentalsShow() {
    SMV(73)
    transposeBy(2)
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.D, Accidental.SHARP, 5, true),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testTransposeByAllTransposingInstrumentsAtConcertAccidentalsNotShown() {
    SCD(instruments = listOf("Trumpet"))
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SMV(74)
    transposeBy(2)
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.E, Accidental.NATURAL, 5, false),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testTransposeByAllStemsChanged() {
    SMV(69)
    transposeBy(2)
    SVP(
      EventType.DURATION,
      EventParam.IS_UPSTEM,
      false,
      eav(1)
    )
  }

  @Test
  fun testTransposeBySimplestEnharmonicOption() {
    SMV()
    transposeBy(1)
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, -5, ez(1))
  }

  @Test
  fun testTransposeBySpecifyFlats() {
    SMV()
    transposeBy(6, accidental = Accidental.FLAT)
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, -6, ez(1))
  }

  @Test
  fun testTransposeBySpecifySharps() {
    SMV()
    transposeBy(6, accidental = Accidental.SHARP)
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 6, ez(1))
  }

  @Test
  fun testTransposeBySpecifySharpsNoteCorrect() {
    SMV(midiVal = 60)
    transposeBy(1, accidental = Accidental.SHARP)
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, Accidental.SHARP), eav(1).copy(id = 1))
  }


  @Test
  fun testTransposeToCorrectEnharmonicOption() {
    SMV()
    transposeTo(7)
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 7, ez(1))
  }

  @Test
  fun testTransposeToCorrectEnharmonicOptionFlats() {
    SMV()
    transposeTo(-7)
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, -7, ez(1))
  }

  @Test
  fun testTransposeByAllStemsCorrectTwoVoices() {
    SMV(69)
    SMV(60, eventAddress = eav(1, dZero(), 2))
    transposeBy(2)
    SVP(
      EventType.DURATION,
      EventParam.IS_UPSTEM,
      true,
      eav(1)
    )
    SVP(
      EventType.DURATION,
      EventParam.IS_UPSTEM,
      false,
      eav(1, dZero(), 2)
    )
  }


  @Test
  fun testTransposeByAllBeamedRetained() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    transposeBy(2)
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1))
  }

  @Test
  fun testTransposeByAllChangesKeyMidScore() {
    SMV()
    SAE(EventType.KEY_SIGNATURE, ez(3), paramMapOf(EventParam.SHARPS to 2))
    transposeBy(2)
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 4, ez(3))
  }

  @Test
  fun testTransposeByRange() {
    SMV()
    transposeBy(2, ez(1), ez(4))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 2, ez(1))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 0, ez(5))
  }

  @Test
  fun testTransposeByRangePitchChanged() {
    SMV()
    SMV(eventAddress = eav(5))
    transposeBy(2, ez(1), ez(4))
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.D, Accidental.NATURAL, 5),
      eav(1).copy(id = 1)
    )
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.C, Accidental.NATURAL, 5),
      eav(5).copy(id = 1)
    )
  }

  @Test
  fun testTransposeToAll() {
    SMV()
    transposeTo(2)
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 2, ez(1))
  }

  @Test
  fun testTransposeToAllFlatKey() {
    SMV()
    transposeTo(-5)
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, -5, ez(1))
  }

  @Test
  fun testTransposeToAllPitchChanged() {
    SMV()
    transposeTo(2)
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.D, Accidental.NATURAL, 5),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testTransposeToAllPitchChangedFlatKey() {
    SMV()
    transposeTo(-5)
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.D, Accidental.FLAT, 5),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testTransposeToAllHarmoniesTransposed() {
    SMV()
    SAE(Harmony(Pitch(NoteLetter.C), "").toEvent())
    transposeTo(2)
    SVP(
      EventType.HARMONY,
      EventParam.TONE,
      Pitch(NoteLetter.D, Accidental.NATURAL, 4),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testTransposeToAllDownPitchChanged() {
    SMV()
    transposeTo(2, up = false)
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.D, Accidental.NATURAL, 4),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testTransposeByAllEveryNoteChanged() {
    SCD(bars = 20)
    repeat(20) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = ea(bar + 1, crotchet() * offset))
      }
    }
    SMV()
    transposeBy(2)
    repeat(20) { bar ->
      repeat(4) { offset ->
        SVP(
          EventType.NOTE,
          EventParam.PITCH,
          Pitch(NoteLetter.D, Accidental.NATURAL, 5),
          eav(bar + 1, crotchet() * offset).copy(id = 1)
        )
      }
    }
  }

  @Test
  fun testTransposeByAllAccidentalsSetEveryBar() {
    SCD(bars = 20)
    repeat(20) { bar ->
      repeat(4) { offset ->
        SMV(73, eventAddress = ea(bar + 1, crotchet() * offset))
      }
    }
    SMV()
    transposeBy(1)
    repeat(20) { bar ->
      SVP(
        EventType.NOTE,
        EventParam.PITCH,
        Pitch(NoteLetter.D, Accidental.NATURAL, 5, true),
        eav(bar + 1).copy(id = 1)
      )
    }
  }

  @Test
  fun testTransposeToRange() {
    SMV()
    transposeTo(2, ez(1), ez(4))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 2, ez(1))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 0, ez(5))
  }

  @Test
  fun testTransposeToRangeLater() {
    SMV()
    transposeTo(2, ez(2), ez(4))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 2, ez(2))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 0, ez(5))
  }


  @Test
  fun testTransposeToRangeStaveIdAndOffset() {
    SMV()
    transposeTo(2, ea(2, crotchet()), ea(4, crotchet()))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 2, ez(2))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 0, ez(5))
  }

  @Test
  fun testTransposePercussionUnchanged() {
    SCD(instruments = listOf("Kit"))
    SMV(midiVal = 35)
    val oldPos =
      EG().getParam<Coord>(EventType.NOTE, EventParam.POSITION, eav(1, voice = 2).copy(id = 1))
    transposeBy(2)
    val newPos =
      EG().getParam<Coord>(EventType.NOTE, EventParam.POSITION, eav(1, voice = 2).copy(id = 1))
    assertEqual(oldPos, newPos)
  }

  private fun transposeBy(
    amount: Int, start: EventAddress = eWild(), end: EventAddress? = null,
    accidental: Accidental? = null
  ) {
    SAE(
      EventType.TRANSPOSE, start,
      paramMapOf(
        EventParam.AMOUNT to amount,
        EventParam.ACCIDENTAL to accidental
      ),
      end
    )
  }

  private fun transposeTo(
    sharps: Int, start: EventAddress = eWild(), end: EventAddress? = null,
    up: Boolean = true
  ) {
    SAE(
      EventType.TRANSPOSE, start,
      paramMapOf(EventParam.SHARPS to sharps, EventParam.IS_UP to up),
      end
    )
  }

}