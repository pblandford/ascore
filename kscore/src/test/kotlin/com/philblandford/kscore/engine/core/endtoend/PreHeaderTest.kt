package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.StaveId
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.core.representation.PREHEADER_GAP
import core.representation.RepTest
import org.junit.Test

class PreHeaderTest : RepTest() {

  @Test
  fun testPartLabel() {
    val partName = getArea("PartName", ea(1).copy(staveId = StaveId(1,0)))
    assert(partName?.area?.width!! > PREHEADER_GAP)
  }
}