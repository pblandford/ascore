package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.pitch.KeySignature
import com.philblandford.kscore.engine.time.TimeSignature
import core.representation.RepTest
import org.junit.Test

class BarStartEndTest : RepTest() {

  @Test
  fun testBarStartCreated() {
    SAE(KeySignature(2).toEvent(), ez(2))
    RVA("BarStart", ea(2))
  }

  @Test
  fun testBarStartAligned() {
    RCD(instruments = listOf("Violin", "Trumpet"))
    SAE(KeySignature(1).toEvent(), ez(2))
    SAE(TimeSignature(3,4).toEvent(), ez(2))
    val violinArea = getArea("TimeSignature", ea(2))!!.coord
    val trumpetArea = getArea("TimeSignature", eas(2, dZero(), StaveId(2,1)))!!.coord
    assertEqual(violinArea.x, trumpetArea.x)
  }

  @Test
  fun testKeyRightOfDoubleBar() {
    SAE(EventType.BARLINE, ez(1), paramMapOf(EventParam.TYPE to BarLineType.DOUBLE))
    SAE(KeySignature(2).toEvent(), ez(2))
    assert(isLeft("BarLine", eas(1,1,0), "KeySignature", ea(2))!!)
  }

}