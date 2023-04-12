package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.paramMapOf

import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.semibreve
import org.junit.Test

class WedgeTest : ScoreTest() {

  @Test
  fun testAddWedge() {
    SAE(EventType.WEDGE, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    SVP(EventType.WEDGE, EventParam.DURATION, semibreve(), ea(1))
  }

  @Test
  fun testAddWedgeBelow() {
    SAE(EventType.WEDGE, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to false))
    SVP(EventType.WEDGE, EventParam.DURATION, semibreve(), ea(1).copy(id=1))
  }

  @Test
  fun testAddWedgeOverlapBefore() {
    SAE(EventType.WEDGE, ea(1), paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to false))
    SAE(EventType.WEDGE, ea(2), paramMapOf(EventParam.END to ea(3), EventParam.IS_UP to false))
    SVP(EventType.WEDGE, EventParam.DURATION, dZero(), ea(1).copy(id=1))
    SVP(EventType.WEDGE, EventParam.DURATION, semibreve(), ea(2).copy(id=1))

  }

}