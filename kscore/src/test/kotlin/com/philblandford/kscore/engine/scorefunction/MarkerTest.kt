
package com.philblandford.kscore.engine.scorefunction


import TestInstrumentGetter
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.types.*
import org.junit.Test
import com.philblandford.kscore.engine.time.TimeSignature
import grace

class MarkerTest : ScoreTest() {
  @Test
  fun testSetMarker() {
    SAE(EventType.UISTATE, ez(0), paramMapOf(EventParam.MARKER_POSITION to ea(1)))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), ez(0))
  }

  @Test
  fun testSetMarkerNotPresent() {
    SDE(EventType.UISTATE, eZero())
    SAE(EventType.UISTATE, ez(0), paramMapOf(EventParam.MARKER_POSITION to ea(1)))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), ez(0))
  }

  @Test
  fun testSetMarkerViaSetNotPresent() {
    SDE(EventType.UISTATE, eZero())
    SSP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), ez(0))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), ez(0))
  }

  @Test
  fun testAddNoteMarkerMoves() {
    SMV()
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, crotchet()), eZero())
  }

  @Test
  fun testAddNoteMarkerMovesGraceTypeNone() {
    SMV(extraParams = paramMapOf(EventParam.GRACE_TYPE to GraceType.NONE))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, crotchet()), eZero())
  }

  @Test
  fun testAddNoteMarkerMovesAcrossBars() {
    SMV(duration = breve())
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(3), eZero())
  }

  @Test
  fun testAddNoteMarkerMovesDotted() {
    SMV(duration = crotchet(1))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, crotchet(1)), eZero())
  }

  @Test
  fun testAddNoteMarkerMovesDottedSecondBeat() {
    SMV()
    SMV(duration = crotchet(1), eventAddress = eav(1, crotchet()))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, crotchet(1).add(crotchet())), eZero())
  }

  @Test
  fun testAddNoteMarkerMovesDottedSecondBeat3_4() {
    SAE(TimeSignature(3, 4).toEvent(), ez(1))
    SMV()
    SMV(duration = crotchet(1), eventAddress = eav(1, crotchet()))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, crotchet(1).add(crotchet())), eZero())
  }

  @Test
  fun testAddNoteMarkerMovesPastBar() {
    sc.setNewScore(Score.create(TestInstrumentGetter(), 4))
    SMV(duration = semibreve())
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(2), eZero())
  }

  @Test
  fun testAddNoteV2MarkerMoves() {
    SMV(eventAddress = eav(1, dZero(),2))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, crotchet()), eZero())
  }

  @Test
  fun testAddNoteV2MarkerMovesShorterDurationInV1() {
    SMV()
    SMV(duration = minim(), eventAddress = eav(1, dZero(),2))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, minim()), eZero())
  }

  @Test
  fun testAddTupletMarkerStays() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), ez(0))
  }

  @Test
  fun testAddNoteTupletMarkerMoves() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SAE(EventType.DURATION, params = dslChord(quaver()).params, eventAddress = eav(1))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, Duration(1, 12)), ez(0))
  }

  @Test
  fun testMarkerEndScore() {
    SAE(EventType.UISTATE, ez(0), paramMapOf(EventParam.MARKER_POSITION to ea(EG().numBars)))
    SMV(duration = semibreve(), eventAddress = eav(EG().numBars))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(EG().numBars), eZero())
  }

  @Test
  fun testDeleteNoteMarkerMoves() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    setMarker(ea(1))
    SDE(EventType.DURATION, eav(1))
    checkMarker(ea(1, crotchet()))
  }

  @Test
  fun testDeleteNoteV2MarkerMoves() {
    SMV(eventAddress = eav(1, voice = 2))
    SMV(eventAddress = eav(1, crotchet(), 2))
    setMarker(ea(1))
    SDE(EventType.DURATION, eav(1, voice = 2))
    checkMarker(ea(1, crotchet()))
  }

  @Test
  fun testDeleteRestV2MarkerMoves() {
    SAE(rest(crotchet()), eventAddress = eav(1, voice = 2))
    setMarker(ea(1))
    SDE(EventType.DURATION, eav(1, voice = 2))
    checkMarker(ea(1, crotchet()))
  }

  @Test
  fun testDeleteLastRestV2MarkerMoves() {
    SAE(rest(semibreve()), eventAddress = eav(1, voice = 2))
    setMarker(ea(1))
    SDE(EventType.DURATION, eav(1, voice = 2))
    checkMarker(ea(2))
  }

  @Test
  fun testDeleteLastRestV2MarkerMovesMinims() {
    SAE(rest(minim()), eventAddress = eav(1, voice = 2))
    SAE(rest(minim()), eventAddress = eav(1, minim(), voice = 2))
    setMarker(ea(1))
    SDE(EventType.DURATION, eav(1, voice = 2))
    SDE(EventType.DURATION, eav(1, minim(), voice = 2))
    checkMarker(ea(2))
  }

  @Test
  fun testDeleteNoteMarkerMovesAfterConsolidating() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    SMV(duration = quaver(), eventAddress = eav(1, crotchet()))
    setMarker(ea(1))
    SDE(EventType.DURATION, eav(1))
    SDE(EventType.DURATION, eav(1, quaver()))
    checkMarker(ea(1, crotchet()))
  }

  @Test
  fun testDeleteNoteTupletMarkerMoves() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SAE(EventType.DURATION, params = dslChord(quaver()).params, eventAddress = eav(1))
    SAE(EventType.DURATION, params = dslChord(quaver()).params, eventAddress = eav(1, Offset(1,12)))
    SDE(EventType.NOTE, eventAddress = eav(1).copy(id = 1))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, Duration(1, 12)), ez(0))
  }

  @Test
  fun testDeleteGraceNoteMarkerMovesToNextGrace() {
    SMV()
    grace()
    grace()
    SDE(EventType.DURATION, eagv(1))
    checkMarker(eag(1, dZero()))
  }

  @Test
  fun testDeleteGraceNoteMarkerMovesGraceToNonGrace() {
    SMV()
    grace()
    SDE(EventType.DURATION, eagv(1))
    checkMarker(ea(1))
  }

  @Test
  fun testDeleteNonGraceNoteMarkerMovesToGrace() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    grace(mainOffset = crotchet())
    SDE(EventType.DURATION, eav(1))
    checkMarker(eag(1, crotchet()))
  }

  @Test
  fun testDeleteNoteMarkerMovesAfterConsolidatingWholeBar() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    setMarker(ea(1))
    SDE(EventType.DURATION, eav(1))
    SDE(EventType.DURATION, eav(1, quaver()))
    checkMarker(ea(2))
  }

  @Test
  fun testDeleteDurationEventRangeMarkerReset() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.UISTATE, eZero(), paramMapOf(EventParam.MARKER_POSITION to ea(1, crotchet())))
    SDE(EventType.DURATION, eav(1), endAddress = eav(1, crotchet()))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), eZero())
  }

  @Test
  fun testDeleteDurationEventRangeMarkerResetIfRestRemoved() {
    SAE(rest())
    SAE(EventType.UISTATE, eZero(), paramMapOf(EventParam.MARKER_POSITION to ea(1, crotchet())))
    SDE(EventType.DURATION, eav(1, crotchet()), endAddress = eav(2))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), eZero())
  }

  @Test
  fun testDeleteAllEventRangeMarkerReset() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.UISTATE, eZero(), paramMapOf(EventParam.MARKER_POSITION to ea(1, crotchet())))
    sc.deleteRange(ea(1), ea(2))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), eZero())
  }

  @Test
  fun testAddNoteMarkerMovesTieToLast() {
    SMV()
    SMV(eventAddress = eav(1,crotchet()), extraParams = paramMapOf(EventParam.TIE_TO_LAST to true))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, minim()), eZero())
  }

  private fun checkMarker(eventAddress: EventAddress) {
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, eventAddress, eZero())
  }

}