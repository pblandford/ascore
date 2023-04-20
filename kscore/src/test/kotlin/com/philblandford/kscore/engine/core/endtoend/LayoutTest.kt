package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class LayoutTest : RepTest() {

  @Test
  fun testPageWidth() {
    SSO(EventParam.LAYOUT_PAGE_WIDTH, BLOCK_HEIGHT*500)
    val page = REP().pages?.first()!!
    assertEqual(BLOCK_HEIGHT*500, page.base.width)
  }

  @Test
  fun testPageHeight() {
    SSO(EventParam.LAYOUT_PAGE_HEIGHT, BLOCK_HEIGHT*500)
    val page = REP().pages?.first()!!
    assertEqual(BLOCK_HEIGHT*500, page.base.height)
  }

  @Test
  fun testPageWidthSinglePart() {
    SSP(EventType.UISTATE, EventParam.SELECTED_PART, 1)
    SSO(EventParam.LAYOUT_PAGE_WIDTH, BLOCK_HEIGHT*500)
    val page = REP().pages?.first()!!
    assertEqual(BLOCK_HEIGHT*500, page.base.width)
  }
}