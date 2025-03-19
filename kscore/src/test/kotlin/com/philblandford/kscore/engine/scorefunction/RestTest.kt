package com.philblandford.kscore.engine.scorefunction

import assertEqual

import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.duration.semibreve
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class RestTest : ScoreTest() {

  @Test
  fun testAddRest() {
    SAE(rest(crotchet()), eav(1))
    SVP(EventType.DURATION, EventParam.DURATION, crotchet(), eav(1))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eav(1))
  }

  @Test
  fun testAddRestDotted() {
    SAE(rest(crotchet(1)), eav(1))
    SVP(EventType.DURATION, EventParam.DURATION, crotchet(1), eav(1))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eav(1))
  }

  @Test
  fun testAddRestAcrossBar() {
    SAE(rest(crotchet()), eav(1))
    SAE(rest(semibreve()), eav(1, minim()))
    SVP(EventType.DURATION, EventParam.DURATION, minim(), eav(1, minim()))
    SVP(EventType.DURATION, EventParam.DURATION, minim(), eav(2))
    SVP(EventType.DURATION, EventParam.DURATION, minim(), eav(2, minim()))
  }

  @Test
  fun testAddRestRequestConsolidate() {
    SMV()
    SAE(rest(crotchet()).addParam(EventParam.CONSOLIDATE to true), eav(1))
    assertEqual("", EG().getVoiceMap(eav(1))?.eventString())
  }

  @Test
  fun testDeleteRestV1Consolidates() {
    SAE(rest(crotchet()), eav(1))
    SDE(EventType.DURATION, eav(1))
    SVP(EventType.DURATION, EventParam.DURATION, minim(), eav(1))
  }

  @Test
  fun testDeleteRestV1ConsolidatesMarkerMoves() {
    SAE(rest(crotchet()), eav(1))
    setMarker(ea(1))
    SDE(EventType.DURATION, eav(1))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, minim()), eZero())
  }

  @Test
  fun testDeleteLastRestMarkerMoves() {
    SAE(rest(minim()), eav(1))
    setMarker(ea(1))
    SDE(EventType.DURATION, eav(1))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(2), eZero())
  }

  @Test
  fun testDeleteRestV2ReplacedByEmpty() {
    SAE(rest(crotchet()), eav(1, dZero(), 2))
    SDE(EventType.DURATION, eav(1, dZero(), 2))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.EMPTY, eav(1, dZero(), 2))
  }

  @Test
  fun testReplaceRestWithEmpty() {
    SAE(rest(crotchet()), eav(1))
    SSP(EventType.DURATION, EventParam.TYPE, DurationType.EMPTY, eav(1))
    SVVM("E4:R4:R2", eav(1))
  }

  @Test
  fun testReplaceEmptyBarRestWithEmpty() {
    SSP(EventType.DURATION, EventParam.TYPE, DurationType.EMPTY, eav(1))
    SVVM("E1", eav(1))
  }

  @Test
  fun testAddEmptyIrregularLengthIsNoop() {
    SAE(EventType.DURATION,eav(1), paramMapOf(
        EventParam.TYPE to DurationType.REST,
        EventParam.DURATION to Duration(1,12)
    ))
    SVVM("", eav(1))
  }
}