package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.core.representation.PAGE_WIDTH
import com.philblandford.kscore.engine.types.StaveId
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.core.representation.STAVE_MARGIN

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class GrandStaveTest : RepTest() {

  @Test
  fun testGrandStaveCreated() {
    RCD(instruments = listOf("Piano"))
    val part = getArea("Part", ea(1).copy(staveId = StaveId(1, 0)))!!.area
    assertEqual(2, part.findByTag("Stave").size)
  }

  @Test
  fun testGrandStaveCreatedDistanceCorrect() {
    RCD(instruments = listOf("Piano"))
    val part = getArea("Part", ea(1).copy(staveId = StaveId(1, 0)))!!.area
    val staves = part.findByTag("StaveLine").toList().sortedBy { it.first.coord.y }
    assertEqual(
      STAVE_MARGIN * 2,
      staves[5].first.coord.y - staves[4].first.coord.y
    )
  }

  @Test
  fun testGrandStaveCreatedDistanceCorrectAfterPageSizeChange() {
    RCD(instruments = listOf("Piano"))
    SSO(EventParam.LAYOUT_PAGE_WIDTH, PAGE_WIDTH + 100)
    val part = getArea("Part", ea(1).copy(staveId = StaveId(1, 0)))!!.area
    val staves = part.findByTag("StaveLine").toList().sortedBy { it.first.coord.y }
    assertEqual(
      STAVE_MARGIN * 2,
      staves[5].first.coord.y - staves[4].first.coord.y
    )
  }
}