package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.api.defaultInstrument
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class InstrumentTest : ScoreTest(){

  @Test
  fun testAddInstrumentBelow() {
    SAE(EventType.PART, ea(2), defaultInstrument().toEvent().addParam(EventParam.NAME, "Trumpet").
      addParam(EventParam.IS_UP, false).params)
    SVP(EventType.INSTRUMENT, EventParam.NAME, "Trumpet", ea(1).copy(staveId = StaveId(2,0)))
  }

  @Test
  fun testSetLabelSetsPartLabel() {
    SSP(EventType.INSTRUMENT, EventParam.LABEL, "Wiible", ea(1))
    assertEqual("Wiible", EG().getPart(1)?.label)
  }

  @Test
  fun testAddInstrumentMidWay() {
    SAE(EventType.INSTRUMENT, ea(2), instrumentGetter.getInstrument("Trumpet")!!.toEvent().params)
    SVP(EventType.INSTRUMENT, EventParam.NAME, "Trumpet", ea(2).copy(staveId = StaveId(1,0)))
  }

  @Test
  fun testAddInstrumentToStave() {
    SAE(EventType.INSTRUMENT, ea(1), instrumentGetter.getInstrument("Trumpet")!!.toEvent().params.plus(EventParam.FOR_STAVE to true))
    SVP(EventType.INSTRUMENT, EventParam.NAME, "Violin", ea(1).copy(staveId = StaveId(1,0)))
    SVP(EventType.INSTRUMENT, EventParam.NAME, "Trumpet", ea(1).copy(staveId = StaveId(1,1)))
  }

  @Test
  fun testAddPianoStaveJoinCreated() {
    SAE(EventType.PART, ea(2), instrumentGetter.getInstrument("Piano")!!.toEvent().addParam(EventParam.IS_UP, false).params)
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 1, ea(0).copy(staveId = StaveId(2,1)))
  }

  @Test
  fun testAddInstrumentVMTimeSignatures() {
    SAE(TimeSignature(3,4).toEvent(), ez(1))
    SAE(EventType.PART, ea(1), defaultInstrument().toEvent().addParam(EventParam.NAME, "Trumpet").
      addParam(EventParam.IS_UP, false).params)
    SMV(eventAddress = eas(1, dZero(), StaveId(2,1)))
    assertEqual("C4:R4:R4", EG().getVoiceMap(easv(1, dZero(), StaveId(2,1)))?.eventString())
  }

  @Test
  fun testAddDeleteInstrument() {
    SAE(EventType.PART, ea(2), defaultInstrument().toEvent().addParam(EventParam.NAME, "Trumpet").
      addParam(EventParam.IS_UP, false).params)
    SDE(EventType.PART, ea(2))
    SVNE(EventType.INSTRUMENT, ea(1).copy(staveId = StaveId(2,0)))
  }

  @Test
  fun testSetInstrumentStart() {
    SAE(EventType.INSTRUMENT, ea(1), defaultInstrument().toEvent().addParam(EventParam.NAME, "Trumpet").
      addParam(EventParam.IS_UP, false).params)
    SVP(EventType.INSTRUMENT, EventParam.NAME, "Trumpet", ea(1).copy(staveId = StaveId(1,0)))
  }

  @Test
  fun testSetInstrumentStartSetsLabel() {
    SAE(EventType.INSTRUMENT, ea(1), defaultInstrument().toEvent().addParam(EventParam.NAME, "Trumpet").
    addParam(EventParam.LABEL, "Trumpet"). addParam(EventParam.IS_UP, false).params)
    SVP(EventType.PART, EventParam.LABEL, "Trumpet", ea(1).copy(staveId = StaveId(1,0)))
  }

  @Test
  fun testSetInstrumentNonTransposingToTransposing() {
    SMV(60)
    SAE(EventType.INSTRUMENT, ea(1), defaultInstrument().toEvent().
    addParam(EventParam.NAME, "Trumpet").addParam(EventParam.TRANSPOSITION, -2).params)
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.D), eav(1).copy(id = 1))
  }

  @Test
  fun testSetInstrumentNonTransposingToTransposingOptionShowConcert() {
    SMV(60)
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SAE(EventType.INSTRUMENT, ea(1), defaultInstrument().toEvent().
    addParam(EventParam.NAME, "Trumpet").addParam(EventParam.TRANSPOSITION, -2).params)
    SVP(EventType.NOTE, EventParam.PITCH, Pitch(NoteLetter.C), eav(1).copy(id = 1))
  }

  @Test
  fun testSetInstrumentStartSetsLabelSinglePartMode() {
    SCD(instruments = listOf("Violin", "Viola"))
    sc.setSelectedPart(1)
    SAE(EventType.INSTRUMENT, ea(1), defaultInstrument().toEvent().addParam(EventParam.NAME, "Trumpet").
    addParam(EventParam.LABEL, "Trumpet").
    addParam(EventParam.IS_UP, false).params)
    SVP(EventType.PART, EventParam.LABEL, "Trumpet", ea(1).copy(staveId = StaveId(1,0)))
  }
}