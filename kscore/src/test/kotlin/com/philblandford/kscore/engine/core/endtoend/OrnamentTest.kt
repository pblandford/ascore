package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.dZero

import core.representation.RepTest
import org.junit.Test

class OrnamentTest : RepTest() {

  @Test
  fun testAddOrnament() {
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    RVA("Ornament", eav(1))
  }

  @Test
  fun testOrnamentAboveDownStem() {
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    assert(isAbove("Ornament", eav(1), "Tadpole", eav(1).copy(id = 1))!!)
  }

  @Test
  fun testOrnamentAboveUpStem() {
    SMV(65)
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    assert(isAbove("Ornament", eav(1), "Stem", eav(1))!!)
  }

  @Test
  fun testOrnamentBelowVoice2() {
    SMV(65, eventAddress = eav(1, dZero(), 2))
    SAE(EventType.ORNAMENT, eav(1, dZero(), 2), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    assert(!isAbove("Ornament", eav(1, dZero(), 2), "Stem", eav(1, dZero(), 2))!!)
  }

  @Test
  fun testAddOrnamentAccidental() {
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL,
      EventParam.ACCIDENTAL_ABOVE to Accidental.FLAT))
    RVA("OrnamentAccidental", eav(1))
  }

  @Test
  fun testAddOrnamentOneAccidental() {
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL,
      EventParam.ACCIDENTAL_ABOVE to Accidental.FLAT))
    assertEqual(1, getAreas("OrnamentAccidental").size)
  }


}