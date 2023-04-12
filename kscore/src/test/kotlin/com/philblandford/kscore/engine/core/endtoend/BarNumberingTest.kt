package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.BarNumbering
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ea

import core.representation.RepTest
import org.junit.Test

class BarNumberingTest : RepTest() {

  @Test
  fun testBarNumberingEverySystem() {
    SSO(EventParam.OPTION_BAR_NUMBERING, BarNumbering.EVERY_SYSTEM)
    val stave2 =
      getAreas("Stave").toList().sortedBy { it.first.eventAddress }[1].first.eventAddress.barNum
    RVA("BarNumber", ea(stave2))
  }

  @Test
  fun testBarNumberingEverySystemNo1() {
    SSO(EventParam.OPTION_BAR_NUMBERING, BarNumbering.EVERY_SYSTEM)
    RVNA("BarNumber", ea(1))
  }

  @Test
  fun testBarNumberingEveryBar() {
    SSO(EventParam.OPTION_BAR_NUMBERING, BarNumbering.EVERY_BAR)
    (2 until EG().numBars).forEach { bar ->
      RVA("BarNumber", ea(bar))
    }
  }

  @Test
  fun testBarNumberingEveryXBars() {
    SSO(EventParam.OPTION_BAR_NUMBERING, 4)
    (4 until EG().numBars step 4).forEach { bar ->
      RVA("BarNumber", ea(bar+1))
    }
  }
}