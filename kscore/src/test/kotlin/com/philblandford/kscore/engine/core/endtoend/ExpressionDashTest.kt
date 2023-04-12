package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.paramMapOf


import core.representation.RepTest
import org.junit.Test

class ExpressionDashTest : RepTest() {

  @Test
  fun testExpressionDash() {
    SAE(EventType.EXPRESSION_DASH, ea(1), paramMapOf(EventParam.IS_UP to true,
      EventParam.END to ea(2),
      EventParam.TEXT to "HWOEHOWI"))
    RVA("ExpressionDash", ea(1))
  }
}