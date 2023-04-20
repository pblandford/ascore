package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.time.TimeSignature

import com.philblandford.kscore.engine.core.representation.RepTest


import org.junit.Test

class TupletTest : RepTest() {

  @Test
  fun testTupletDrawn() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent(), eav(1))
    RVA("Tuplet", eav(1))
  }

  @Test
  fun testTupletLinesDrawn() {
    SAE(tuplet(dZero(), 3, minim()).toEvent(), eav(1))
    assertEqual(2, getAreas("TupletLine").size)
  }

  @Test
  fun testTupletLinesNotDrawnBeams() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent(), eav(1))
    assertEqual(0, getAreas("TupletLine").size)
  }

  @Test
  fun testHiddenTuplet() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent().addParam(EventParam.HIDDEN to true), eav(1))
    assertEqual(0, getAreas("Tuplet").size)
  }

  @Test
  fun testTupletDrawnUnderVoice2() {
    SAE(rest(), eav(1, voice = 2))
    SAE(tuplet(dZero(), 3, crotchet()).toEvent(), eav(1, voice = 2))
    assert(isAbove("Rest", eav(1, voice = 2), "Tuplet", eav(1, voice = 2)) == true)
  }


  @Test
  fun testAddQuadrupletNotesBeams() {
    SAE(TimeSignature(12, 8).toEvent(), ez(1))
    SAE(rest(crotchet(1)))
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 4)
    )
    repeat(4) {
      SMV(
        duration = semiquaver(),
        eventAddress = eav(1, Duration(3, 32).multiply(it))
      )
    }
    RVA("Beam", eav(1))
  }
}