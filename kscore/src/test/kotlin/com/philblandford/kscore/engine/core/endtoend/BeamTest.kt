package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.representation.BEAM_MAX_GRADIENT
import com.philblandford.kscore.engine.core.representation.LINE_THICKNESS
import com.philblandford.kscore.engine.core.representation.RepTest


import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.time.TimeSignature
import grace
import org.junit.Test
import kotlin.math.abs

class BeamTest : RepTest() {

  @Test
  fun testBeamCreated() {
    SMV(duration = quaver(), eventAddress = eav(1))
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    RVA("Beam", eav(1))
  }

  @Test
  fun testBeamNotCreatedGaps() {
    SMV(duration = quaver(), eventAddress = eav(1))
    SMV(duration = quaver(), eventAddress = eav(1, crotchet()))
    RVNA("Beam", eav(1))
  }

  @Test
  fun testBeamCreatedVoice2() {
    SMV(duration = quaver(), eventAddress = eav(1, voice = 2))
    SMV(duration = quaver(), eventAddress = eav(1, quaver(), 2))
    RVA("Beam", eav(1, voice = 2))
  }

  @Test
  fun testBeamCreatedTailsGone() {
    SMV(duration = quaver(), eventAddress = eav(1))
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    RVNA("Tail", eav(1))
    RVNA("Tail", eav(1, quaver()))
  }

  @Test
  fun testBeamCreatedTuplet() {
    SAE(EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8))
    repeat(3) {
      SMV(duration = quaver(),
        eventAddress = eav(1, Offset(1,12).multiply(it)))
    }
    RVA("Beam", eav(1))
  }

  @Test
  fun testBeamCreatedTupleTwoNotes() {
    SAE(EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8))
    repeat(2) {
      SMV(duration = quaver(),
        eventAddress = eav(1, Offset(1,12).multiply(it)))
    }
    RVA("Beam", eav(1))
  }

  @Test
  fun testBeamCreatedTupleTwoNotesAfterRest() {
    SAE(EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8))
    repeat(2) {
      SMV(duration = quaver(),
        eventAddress = eav(1, Offset(1,12).multiply(it+1)))
    }
    RVA("Beam", eav(1, Offset(1,12)))
  }

  @Test
  fun testBeamCreatedTupletVoice2() {
    SAE(EventType.TUPLET, eav(1, dZero(), 2), paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8))
    repeat(3) {
      SMV(duration = quaver(),
        eventAddress = eav(1, Offset(1,12).multiply(it), 2))
    }
    RVA("Beam", eav(1, dZero(), 2))
  }

  @Test
  fun testNoTailsTupletTwoNotesAfterRest() {
    SAE(EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8))
    repeat(2) {
      SMV(duration = quaver(),
        eventAddress = eav(1, Offset(1,12).multiply(it+1)))
    }
    RVNA("Tail", eav(1, Offset(1,12)))
  }

  @Test
  fun testBeamCreatedMultipleTuplets() {
    SAE(EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8))
    SAE(EventType.TUPLET, eav(1, crotchet()), paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8))
    repeat(6) {
      SMV(duration = quaver(),
        eventAddress = eav(1, Offset(1,12).multiply(it)))
    }
    RVA("Beam", eav(1))
    RVA("Beam", eav(1, crotchet()))
  }

  @Test
  fun testBeamWithinMaxGradient() {
    SMV(60, duration = quaver(), eventAddress = eav(1))
    SMV(75, duration = quaver(), eventAddress = eav(1, quaver()))
    val area = getArea("Beam", eav(1))!!.area
    val gradient = area.height.toFloat() / area.width
    assert(abs(gradient) <= BEAM_MAX_GRADIENT)
  }

  @Test
  fun testBeamMovesWithStemExtension() {
    SMV(67, duration = quaver(), eventAddress = eav(1))
    SMV(67, duration = quaver(), eventAddress = eav(1, quaver()))
    SMV(72, duration = quaver(), eventAddress = eav(1, crotchet()))
    SMV(64, duration = quaver(), eventAddress = eav(1, crotchet(1)))
    val beam = getArea("Beam", eav(1))!!.coord
    val stem = getArea("Stem", eav(1))!!.coord
    assertEqual(beam.y, stem.y)
  }
  
  @Test
  fun testBeamCreatedDottedNotes6_8() {
    SAE(TimeSignature(6,8).toEvent(), ez(1))
    SMV(duration = quaver(1), eventAddress = eav(1))
    SMV(duration = semiquaver(), eventAddress = eav(1, quaver(1)))
    SMV(duration = quaver(), eventAddress = eav(1, crotchet()))
    RVA("Beam", eav(1))
  }

  @Test
  fun testTwoSingleBeamsCreatedDottedNotes6_8() {
    SAE(TimeSignature(6,8).toEvent(), ez(1))
    SMV(duration = quaver(1), eventAddress = eav(1))
    SMV(duration = semiquaver(), eventAddress = eav(1, quaver(1)))
    SMV(duration = quaver(), eventAddress = eav(1, crotchet()))
    val singleBeams = getAreas("Beam")
    assertEqual(2, singleBeams.size)
    singleBeams.forEach{
      assert(it.value.width > 0)
    }
  }

  @Test
  fun testStemPositionsTwoVoicesCollision() {
    SMV(65, quaver())
    SMV(64, quaver(), eventAddress = eav(1,voice = 2))
    SMV(65, quaver(), eventAddress = eav(1, quaver()))
    SMV(64, quaver(), eventAddress = eav(1, quaver(), 2))
    val tadpole2 = getArea("Tadpole", eav(1, voice = 2).copy(id = 1))!!
    val stem2 = getArea("Stem", eav(1, voice = 2))!!
    assertEqual(tadpole2.coord.x + LINE_THICKNESS/2, stem2.coord.x)
  }

  @Test
  fun testBeamCreatedGrace() {
    grace()
    grace()
    RVA("Beam", eagv(1))
  }

  @Test
  fun testBeamCreatedGraceBar2() {
    grace(bar = 2)
    grace(bar = 2)
    RVA("Beam", eagv(2))
  }

  @Test
  fun testBeamGraceHasWidth() {
    grace()
    grace()
    val area = getArea("Beam", eagv(1))!!.area
    assert(area.width > 0)
  }

  @Test
  fun testBeamCreatedGraceSecondCrotchet() {
    SMV()
    grace(mainOffset = crotchet())
    grace(semiquaver(), mainOffset = crotchet())
    RVA("Beam", eagv(1, crotchet()))
  }

  @Test
  fun testBeamCreatedGraceSecondCrotchetHasWidth() {
    SMV()
    grace(mainOffset = crotchet())
    grace(semiquaver(), mainOffset = crotchet())
    val area = getArea("Beam", eagv(1, crotchet()))!!.area
    assert(area.width > 0)
  }

  @Test
  fun testBeamStemsCorrect() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    val stem = getArea("Stem", eav(1))!!.coord
    val tadpole = getArea("Tadpole", eav(1).copy(id = 1))!!.coord
    assertEqual(tadpole.x + LINE_THICKNESS/2, stem.x)
  }

  @Test
  fun testBeamStemsCorrectLedgers() {
    SMV(84, duration = quaver())
    SMV(84, duration = quaver(), eventAddress = eav(1, quaver()))
    val stem = getArea("Stem", eav(1))!!.coord
    val tadpole = getArea("Tadpole", eav(1).copy(id = 1))!!.coord
    assertEqual(tadpole.x + LINE_THICKNESS/2, stem.x)
  }


}