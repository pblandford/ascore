package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.duration.*


import org.junit.Test

class OctaveTest : ScoreTest() {

  @Test
  fun testAddOctave() {
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2)
      )
    )
    SVP(EventType.OCTAVE, EventParam.DURATION, semibreve(), ea(1))
  }

  @Test
  fun testGetOctaveVoiceIgnored() {
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2)
      )
    )
    SVP(EventType.OCTAVE, EventParam.DURATION, semibreve(), eav(1))
  }

  @Test
  fun testAddOctaveNoteChanged() {
    SMV(60)
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2)
      )
    )
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 5), eav(1).copy(id = 1))
  }

  @Test
  fun testAddOctaveTwoNoteChanged() {
    SMV(60)
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2), EventParam.NUMBER to 2
      )
    )
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 6), eav(1).copy(id = 1))
  }

  @Test
  fun testAddOctaveNotesChangedOverBar() {
    repeat(4) { o ->
      SMV(60, eventAddress = eav(1, crotchet() * o))
    }
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2)
      )
    )
    repeat(4) { o ->
      SVP(
        EventType.NOTE,
        EventParam.PITCH,
        Pitch(NoteLetter.C, octave = 5),
        eav(1, crotchet() * o).copy(id = 1)
      )

    }
  }


  @Test
  fun testAddOctaveBelowNoteChanged() {
    SMV(60)
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to -1,
        EventParam.END to ea(2)
      )
    )
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 3), eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteOctaveNoteChanged() {
    SMV(60)
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ea(2)))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 5), eav(1).copy(id = 1))
    SDE(EventType.OCTAVE, ea(1))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 4), eav(1).copy(id = 1))
  }

  @Test
  fun testAddOctaveNewNotePosition() {
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ea(2)))
    SMV(60)
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 10), eav(1).copy(id = 1))
  }

  @Test
  fun testAddOctaveNewNotePitch() {
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ea(2)))
    SMV(60)
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 5), eav(1).copy(id = 1))
  }

  @Test
  fun testAddOctaveNewNotePitchStaff2() {
    SCD(instruments = listOf("Piano"))
    SAE(
      EventType.OCTAVE, eas(1, staveId = StaveId(1, 2)),
      paramMapOf(EventParam.NUMBER to 1, EventParam.END to eas(2, staveId = StaveId(1, 2)))
    )
    SMV(60)
    SMV(60, eventAddress = eas(1, staveId = StaveId(1, 2)))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 4), eav(1).copy(id = 1))
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.C, octave = 5),
      eav(1).copy(id = 1, staveId = StaveId(1, 2))
    )
  }

  @Test
  fun testAddOctaveNewNotePositionStaff2() {
    SCD(instruments = listOf("Piano"))
    SAE(
      EventType.OCTAVE, eas(1, staveId = StaveId(1, 2)),
      paramMapOf(EventParam.NUMBER to 1, EventParam.END to eas(2, staveId = StaveId(1, 2)))
    )
    SMV(60)
    SMV(60, eventAddress = eas(1, staveId = StaveId(1, 2)))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 10), eav(1).copy(id = 1))
    SVP(
      EventType.NOTE,
      EventParam.POSITION,
      Coord(0, -2),
      eav(1).copy(id = 1, staveId = StaveId(1, 2))
    )
  }

  @Test
  fun testAddOctaveNoteChangedTwoStaves() {
    SCD(instruments = listOf("Piano"))
    SMV(60)
    SMV(
      60, eventAddress = eav(1).copy(
        staveId = StaveId(
          1, 2
        )
      )
    )
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2)
      )
    )
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 5), eav(1).copy(id = 1))
  }

  @Test
  fun testAddOctavePositionSameTwoStaves() {
    SCD(instruments = listOf("Piano"))
    SMV(60)
    SMV(
      60, eventAddress = eav(1).copy(
        staveId = StaveId(
          1, 2
        )
      )
    )
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2)
      )
    )
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 10), eav(1).copy(id = 1))
  }

  @Test
  fun testSetOctaveNumber() {
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2)
      )
    )
    SSP(EventType.OCTAVE, EventParam.NUMBER, 2, ea(1))
    SVP(EventType.OCTAVE, EventParam.NUMBER, 2, ea(1))
  }

  @Test
  fun testSetParamAfterOctave() {
    repeat(3) {bar ->
      repeat(4) { offset ->
        SMV(72, eventAddress =  eav(bar +1, crotchet() * offset))
      }
    }
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2, minim(1))
      )
    )
    SAE(EventType.TREMOLO, eav(3), paramMapOf(EventParam.TREMOLO_BEATS to semiquaver()))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 5), eav(3).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0,3), eav(3).copy(id = 1))
  }

  @Test
  fun testSetParamAfterOctaveBelow() {
    repeat(3) {bar ->
      repeat(4) { offset ->
        SMV(72, eventAddress =  eav(bar +1, crotchet() * offset))
      }
    }
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2, minim(1))
      )
    )
    SAE(EventType.TREMOLO, eav(3), paramMapOf(EventParam.TREMOLO_BEATS to semiquaver()))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 5), eav(3).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0,3), eav(3).copy(id = 1))
  }

  @Test
  fun testSetParamAfterOctaveLastNoteOfBar() {
    repeat(3) {bar ->
      repeat(4) { offset ->
        SMV(72, eventAddress =  eav(bar +1, crotchet() * offset))
      }
    }
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2, minim(1))
      )
    )
    SAE(EventType.TREMOLO, eav(2, minim(1)), paramMapOf(EventParam.TREMOLO_BEATS to semiquaver()))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 6), eav(2, minim(1)).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0,3), eav(2, minim(1)).copy(id = 1))
  }

  @Test
  fun testAddNoteAfterOctave() {
    SAE(
      EventType.OCTAVE, ea(1), paramMapOf(
        EventParam.NUMBER to 1,
        EventParam.END to ea(2)
      )
    )
    SMV(eventAddress = eav(3))
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C, octave = 5), eav(3).copy(id = 1))
  }

}