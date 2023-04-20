package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT


import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class SegmentTest : RepTest() {

  @Test
  fun testTadpoleStem() {
    SMV()
    val tadpole = getArea("Tadpole", eav(1).copy(id = 1))!!
    val stem = getArea("Stem", eav(1))!!
    assert(tadpole.coord.y + BLOCK_HEIGHT == stem.coord.y)
  }

  @Test
  fun testTadpoleStemUpStem() {
    SMV(60)
    val tadpole = getArea("Tadpole", eav(1).copy(id = 1))!!
    val stem = getArea("Stem", eav(1))!!
    assert(tadpole.coord.y + BLOCK_HEIGHT == stem.coord.y + stem.area.height)
  }

  @Test
  fun testAccidentalCreated() {
    SMV(73, accidental = Accidental.SHARP)
    val tadpole = getArea("Tadpole", eav(1).copy(id = 1))!!
    val accidental = getArea("AccidentalArea", ea(1))!!
    assert(tadpole.coord.x >= accidental.coord.x + accidental.area.width)
  }

  @Test
  fun testAccidentalCreatedCrossVoices() {
    SMV(73, accidental = Accidental.SHARP)
    SMV(78, eventAddress = eav(1, voice = 2))
    val v2Stem = getArea("Stem", eav(1, voice  = 2))!!
    val accidental = getArea("AccidentalArea", ea(1))!!
    assert(accidental.coord.x + accidental.area.width <= v2Stem.coord.x)
  }
}