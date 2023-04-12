package com.philblandford.kscore.engine.scorefunction

import assertEqual

import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.types.*
import grace
import org.junit.Test

class StemTest : ScoreTest() {

  @Test
  fun testSetStemDirection() {
    SMV()
    SSP(EventType.DURATION, EventParam.IS_UPSTEM, true, eav(1))
    SVB(EventType.DURATION, EventParam.IS_UPSTEM, true, eav(1))
  }

  @Test
  fun testSetStemDirectionRange() {
    repeat(4) {
      SMV(eventAddress = eav(1, crotchet().multiply(it)))
    }
    SSP(EventType.DURATION, EventParam.IS_UPSTEM, true, eav(1), eav(1, minim(1)))
    repeat(4) {
      SVB(EventType.DURATION, EventParam.IS_UPSTEM, true, eav(1, crotchet().multiply(it)))
    }
  }

  @Test
  fun testUnSetStemDirection() {
    SMV()
    SSP(EventType.DURATION, EventParam.IS_UPSTEM, true, eav(1))
    SSP(EventType.DURATION, EventParam.IS_UPSTEM, null, eav(1))
    SVB(EventType.DURATION, EventParam.IS_UPSTEM, false, eav(1))
  }

  @Test
  fun testSetStemDirectionGrace() {
    grace()
    SSP(EventType.DURATION, EventParam.IS_UPSTEM, true, eagv(1))
    SVB(EventType.DURATION, EventParam.IS_UPSTEM, true, eagv(1))
  }

  @Test
  fun testSetStemDirectionGraceSlashRetained() {
    grace(type = GraceType.ACCIACCATURA)
    SVB(EventType.DURATION, EventParam.IS_SLASH, true, eagv(1))
    SSP(EventType.DURATION, EventParam.IS_UPSTEM, true, eagv(1))
    SVB(EventType.DURATION, EventParam.IS_SLASH, true, eagv(1))
  }

  @Test
  fun testSetStemDirectionRangeGrace() {
    grace()
    grace()
    repeat(2) {
      SVB(
        EventType.DURATION,
        EventParam.IS_UPSTEM_BEAM,
        false,
        eagv(1, graceOffset = semiquaver().multiply(it))
      )
    }
    SSP(
      EventType.DURATION,
      EventParam.IS_UPSTEM,
      true,
      eagv(1),
      eagv(1, graceOffset = semiquaver())
    )
    repeat(2) {
      SVB(
        EventType.DURATION,
        EventParam.IS_UPSTEM_BEAM,
        true,
        eagv(1, graceOffset = semiquaver().multiply(it))
      )
    }
  }

  @Test
  fun testSetStemDirectionRangeGraceBeamStems() {
    grace()
    grace()
    SSP(
      EventType.DURATION,
      EventParam.IS_UPSTEM,
      false,
      eagv(1),
      eagv(1, graceOffset = semiquaver())
    )
    val beam = EG().getBeams().toList().first()
    assertEqual(false, beam.second.up)
  }

  @Test
  fun testSetStemDirectionBeamedNotes() {
    SMV(60, duration = quaver())
    SMV(60, duration = quaver(), eventAddress = ea(1, quaver()))
    SSP(EventType.DURATION, EventParam.IS_UPSTEM, false, eav(1), eav(1, quaver()))
    val beam = EG().getBeams().toList().first()
    assertEqual(false, beam.second.up)
  }

  @Test
  fun testSetStemDirectionOneInGroupOfBeamedNotes() {
    repeat(4) {
      SMV(60, duration = quaver(), eventAddress = ea(1, quaver() * it))
    }
    SSP(EventType.DURATION, EventParam.IS_UPSTEM, false, eav(1), eav(1, quaver()))
    assert(EG().getEvent(EventType.DURATION, eav(1))?.isUpstem() == true)
  }

}