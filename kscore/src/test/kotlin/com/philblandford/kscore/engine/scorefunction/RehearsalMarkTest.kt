package com.philblandford.kscore.engine.scorefunction


import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class RehearsalMarkTest : ScoreTest() {

  @Test
  fun testAddRehearsalMark() {
    SAE(EventType.REHEARSAL_MARK, ea(1), paramMapOf(EventParam.TEXT to "Wibble"))
    SVP(EventType.REHEARSAL_MARK, EventParam.TEXT, "Wibble", ez(1))
  }

  @Test
  fun testAddRehearsalMarkOffsetIgnored() {
    SAE(EventType.REHEARSAL_MARK, ea(1, minim()), paramMapOf(EventParam.TEXT to "Wibble"))
    SVE(EventType.REHEARSAL_MARK, ez(1))
    assert(SCORE().eventMap.getEvent(EventType.REHEARSAL_MARK, ez(1, minim())) == null)
  }
}