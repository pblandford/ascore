package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*

import com.philblandford.kscore.engine.duration.semibreve
import org.junit.Test

class LongTrillTest : ScoreTest() {

  @Test
  fun testAddLongTrill() {
    SAE(EventType.LONG_TRILL, ea(1), paramMapOf(EventParam.IS_UP to true,
      EventParam.END to ea(2)))
    SVP(EventType.LONG_TRILL, EventParam.DURATION, semibreve(), ea(1))
  }

  @Test
  fun testGetLongTrillVoiceIgnored() {
    SAE(EventType.LONG_TRILL, ea(1), paramMapOf(EventParam.IS_UP to true,
      EventParam.END to ea(2)))
    SVP(EventType.LONG_TRILL, EventParam.DURATION, semibreve(), eav(1))
  }


}