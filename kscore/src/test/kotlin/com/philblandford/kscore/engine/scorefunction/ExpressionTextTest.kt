package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*
import org.junit.Test

class ExpressionTextTest : ScoreTest() {

  @Test
  fun testAddExpression() {
    SAE(EventType.EXPRESSION_TEXT, ea(1), paramMapOf(EventParam.TEXT to "Cresc",
      EventParam.IS_UP to true))
    SVE(EventType.EXPRESSION_TEXT, ea(1))
  }

  @Test
  fun testAddExpressionDown() {
    SAE(EventType.EXPRESSION_TEXT, ea(1), paramMapOf(EventParam.TEXT to "Cresc",
      EventParam.IS_UP to false))
    SVE(EventType.EXPRESSION_TEXT, ea(1).copy(id = 1))
  }


}