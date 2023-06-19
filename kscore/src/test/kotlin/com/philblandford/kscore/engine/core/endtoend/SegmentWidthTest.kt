package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.core.representation.RepTest
import com.philblandford.kscore.engine.core.representation.getArea
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.paramMapOf
import org.junit.Test

class SegmentWidthTest : RepTest() {
  @Test
  fun testSetSegmentWidth() {
    SMV()
    val original = REP().getArea("Segment", ea(1))!!.second.width
    SAE(EventType.SPACE, ea(1), paramMapOf(EventParam.AMOUNT to 20))
    val new = REP().getArea("Segment", ea(1))!!.second.width
    assertEqual(original + 22, new)
  }
}