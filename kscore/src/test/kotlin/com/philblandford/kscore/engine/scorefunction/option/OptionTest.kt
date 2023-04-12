package com.philblandford.kscore.engine.scorefunction.option

import assertEqual
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eav
import org.junit.Test

class OptionTest : ScoreTest() {

  @Test
  fun testSetOption() {
    SSO(EventParam.OPTION_BARS_PER_LINE, 20)
    assertEqual(20, SGO(EventParam.OPTION_BARS_PER_LINE))
  }

  @Test
  fun testSetOptionBoolean() {
    SSO(EventParam.OPTION_SHOW_MULTI_BARS, true)
    assertEqual(true, SGO(EventParam.OPTION_SHOW_MULTI_BARS))
  }

  @Test
  fun testSetOptionVoiceAddress() {
    SSP(EventType.OPTION,  EventParam.OPTION_BARS_PER_LINE, 20, eav(1))
    assertEqual(20, SGO(EventParam.OPTION_BARS_PER_LINE))
  }

  @Test
  fun testSetLayoutOption() {
    SSO(EventParam.LAYOUT_PAGE_WIDTH, 2000)
    assertEqual(2000, SGO(EventParam.LAYOUT_PAGE_WIDTH))
  }

  @Test
  fun testSetLayoutOptionOthersUnaffected() {
    SSO(EventParam.LAYOUT_STAVE_GAP, 200)
    SSO(EventParam.LAYOUT_PAGE_WIDTH, 2000)
    assertEqual(200, SGO(EventParam.LAYOUT_STAVE_GAP))
  }


}