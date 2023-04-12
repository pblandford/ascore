package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.paramMapOf

import com.philblandford.kscore.engine.duration.semibreve
import org.junit.Test

class ExpressionDashTest : ScoreTest() {

  @Test
  fun testAddExpressionDash() {
    SAE(
      EventType.EXPRESSION_DASH, ea(1), paramMapOf(
        EventParam.TEXT to "cresc",
        EventParam.IS_UP to true, EventParam.END to ea(2)
      )
    )
    SVP(EventType.EXPRESSION_DASH, EventParam.DURATION, semibreve(), ea(1))
  }

  @Test
  fun testAddExpressionDashAsRange() {
    SAE(
      EventType.EXPRESSION_DASH, ea(1), paramMapOf(
        EventParam.TEXT to "cresc",
        EventParam.IS_UP to true
      ), endAddress = ea(2)
    )
    SVP(EventType.EXPRESSION_DASH, EventParam.DURATION, semibreve(), ea(1))
  }
}