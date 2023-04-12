package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.representation.REHEARSAL_MARK_SIZE



import core.representation.RepTest
import org.junit.Test

class RehearsalMarkTest : RepTest() {

  @Test
  fun testAddRehearsalMark() {
    SAE(EventType.REHEARSAL_MARK, ea(1), paramMapOf(EventParam.TEXT to "Wibble"))
    RVA("RehearsalMark", ez(1))
  }


  @Test
  fun testMoveRehearsalMark() {
    SAE(EventType.REHEARSAL_MARK, ea(1), paramMapOf(EventParam.TEXT to "Wibble"))
    val before = getArea("RehearsalMark", ez(1))!!.coord
    SSP(EventType.REHEARSAL_MARK, EventParam.HARD_START, Coord(20,0), ez(1))
    val after = getArea("RehearsalMark", ez(1))!!.coord
    assertEqual(before.x + 20, after.x)
  }

  @Test
  fun testSetTextSizeRehearsalMark() {
    SAE(EventType.REHEARSAL_MARK, ez(1), paramMapOf(EventParam.TEXT to "Wibble"))
    val before = getArea("RehearsalMark", ez(1))!!.area.width
    val current = EG().getParam<Int>(EventType.REHEARSAL_MARK, EventParam.TEXT_SIZE) ?: REHEARSAL_MARK_SIZE
    SSP(EventType.REHEARSAL_MARK, EventParam.TEXT_SIZE, current+50, ez(1))
    val after = getArea("RehearsalMark", ez(1))!!.area.width
    assert(after > before)
  }
}