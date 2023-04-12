package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.area.Coord

import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.duration.*
import org.junit.Test

class ClefTest : ScoreTest() {

  @Test
  fun testAddClef() {
    SAE(EventType.CLEF, params = paramMapOf(EventParam.TYPE to ClefType.BASS))
    SVP(EventType.CLEF, EventParam.TYPE, ClefType.BASS, ea(1))
  }

  @Test
  fun testAddClefNotesMove() {
    SMV(60)
    SAE(EventType.CLEF, params = paramMapOf(EventParam.TYPE to ClefType.BASS))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, -2), eav(1).copy(id = 1))
  }

  @Test
  fun testAddClefNotesMoveWholeBar() {
    repeat(4) {
      SMV(60, eventAddress = eav(1, crotchet() * it))
    }
    SAE(EventType.CLEF, params = paramMapOf(EventParam.TYPE to ClefType.BASS))
    repeat(4) {
      SVP(EventType.NOTE, EventParam.POSITION, Coord(0, -2), eav(1, crotchet() * it).copy(id = 1))
    }
  }

  @Test
  fun testAddClefNotesMoveOnlyToNextClef() {
    SAE(EventType.CLEF, ea(3), paramMapOf(EventParam.TYPE to ClefType.BASS))
    SMV(60)
    SMV(60, eventAddress = eav(2))
    SMV(60, eventAddress = eav(3))
    SAE(EventType.CLEF, ea(2), paramMapOf(EventParam.TYPE to ClefType.ALTO))

    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 10), eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 4), eav(2).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, -2), eav(3).copy(id = 1))
  }

  @Test
  fun testAddClefNotesMoveOnlyToNextClefMidBar() {
    SAE(EventType.CLEF, ea(3, minim()), paramMapOf(EventParam.TYPE to ClefType.BASS))
    SMV(60)
    SMV(60, eventAddress = eav(2))
    SMV(60, eventAddress = eav(3))
    SMV(60, eventAddress = eav(3, minim()))
    SAE(EventType.CLEF, ea(2), paramMapOf(EventParam.TYPE to ClefType.ALTO))

    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 10), eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 4), eav(2).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 4), eav(3).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, -2), eav(3, minim()).copy(id = 1))
  }

  @Test
  fun testAddClefNotesMoveMidTuplet() {
    SAE(tuplet(dZero(), 3, 8).toEvent(), eav(1))
    repeat(3) {
      SMV(duration = quaver(), eventAddress = eav(1, Duration(1, 12).multiply(it)))
    }
    SAE(
      EventType.CLEF, eav(1, Duration(1, 6)),
      paramMapOf(EventParam.TYPE to ClefType.BASS)
    )
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 3), eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 3), eav(1, Duration(1, 12)).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, -9), eav(1, Duration(1, 6)).copy(id = 1))
  }

  @Test
  fun testAddClefThenNotesPositionsCorrect() {
    SAE(tuplet(dZero(), 3, 8).toEvent(), eav(1))
    SAE(
      EventType.CLEF, eav(1, Duration(1, 6)),
      paramMapOf(EventParam.TYPE to ClefType.BASS)
    )
    repeat(3) {
      SMV(duration = quaver(), eventAddress = eav(1, Duration(1, 12).multiply(it)))
    }
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 3), eav(1).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 3), eav(1, Duration(1, 12)).copy(id = 1))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, -9), eav(1, Duration(1, 6)).copy(id = 1))
  }

  @Test
  fun testAddClefMidBar() {
    SAE(
      EventType.CLEF,
      params = paramMapOf(EventParam.TYPE to ClefType.BASS),
      eventAddress = ea(1, crotchet())
    )
    SVP(EventType.CLEF, EventParam.TYPE, ClefType.BASS, ea(1, crotchet()))
  }

  @Test
  fun testAddClefMidBarOriginalStillThere() {
    SAE(
      EventType.CLEF,
      params = paramMapOf(EventParam.TYPE to ClefType.BASS),
      eventAddress = ea(1, crotchet())
    )
    SVP(EventType.CLEF, EventParam.TYPE, ClefType.BASS, ea(1, crotchet()))
    SVA(EventType.CLEF, ea(1))
  }

  @Test
  fun testAddClefVoiceRemoved() {
    SAE(EventType.CLEF, eav(2), paramMapOf(EventParam.TYPE to ClefType.BASS))
    SVP(EventType.CLEF, EventParam.TYPE, ClefType.BASS, ea(2))
  }

  @Test
  fun testAddClefStemsChange() {
    SMV(60)
    SAE(EventType.CLEF, params = paramMapOf(EventParam.TYPE to ClefType.BASS))
    SVB(EventType.DURATION, EventParam.IS_UPSTEM, false, eav(1))
  }

  @Test
  fun testAddClefAfterSame() {
    SAE(EventType.CLEF, eav(2), params = paramMapOf(EventParam.TYPE to ClefType.TREBLE))
    SVNE(EventType.CLEF, ea(2))
  }

  @Test
  fun testAddClefRemovesSameLater() {
    SAE(EventType.CLEF, eav(4), params = paramMapOf(EventParam.TYPE to ClefType.ALTO))
    SAE(EventType.CLEF, eav(2), params = paramMapOf(EventParam.TYPE to ClefType.ALTO))
    SVNE(EventType.CLEF, ea(4))
  }

  @Test
  fun testAddClefLeavesDifferentLater() {
    SAE(EventType.CLEF, eav(4), params = paramMapOf(EventParam.TYPE to ClefType.BASS))
    SAE(EventType.CLEF, eav(2), params = paramMapOf(EventParam.TYPE to ClefType.ALTO))
    SVE(EventType.CLEF, ea(4))
  }

  @Test
  fun testAddClefToPercussionIsNoOp() {
    SCD(instruments = listOf("Kit"))
    SAE(EventType.CLEF, ea(2), params = paramMapOf(EventParam.TYPE to ClefType.ALTO))
    SVNE(EventType.CLEF, ea(2))
  }

  @Test
  fun testDeleteClef() {
    SAE(EventType.CLEF, ea(2), params = paramMapOf(EventParam.TYPE to ClefType.BASS))
    SDE(EventType.CLEF, ea(2))
    SVNE(EventType.CLEF, ea(2))
  }

  @Test
  fun testDeleteFirstClefIsNoop() {
    SDE(EventType.CLEF, ea(1))
    SVE(EventType.CLEF, ea(1))
  }

  @Test
  fun testDeleteClefNotesMove() {
    SMV(60, eventAddress = eav(2))
    SAE(EventType.CLEF, ea(2), paramMapOf(EventParam.TYPE to ClefType.BASS))
    SDE(EventType.CLEF, ea(2))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, 10), eav(2).copy(id = 1))
  }

  @Test
  fun testDeleteClefNotesOriginalBass() {
    SAE(EventType.CLEF, ea(1), paramMapOf(EventParam.TYPE to ClefType.BASS))
    SMV(60, eventAddress = eav(2))
    SAE(EventType.CLEF, ea(2), paramMapOf(EventParam.TYPE to ClefType.ALTO))
    SDE(EventType.CLEF, ea(2))
    SVP(EventType.NOTE, EventParam.POSITION, Coord(0, -2), eav(2).copy(id = 1))
  }

  @Test
  fun testDeleteClefStemsChange() {
    SMV(60)
    SAE(EventType.CLEF, params = paramMapOf(EventParam.TYPE to ClefType.BASS))
    SVB(EventType.DURATION, EventParam.IS_UPSTEM, false, eav(1))
  }
}