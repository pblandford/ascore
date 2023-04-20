package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class PartTest : RepTest() {

  @Test
  fun testSetPartLabel() {
    SSP(EventType.PART, EventParam.LABEL, "Wibble", eas(1, dZero(), StaveId(1,0)))
    val area = getArea("PartName", ea(1).copy(staveId = StaveId(1,0)))?.area!!
    val text = (area.drawable as TestDrawable).text
    assertEqual("Wibble", text)
  }
}