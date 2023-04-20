package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.ez
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class RepeatTest : RepTest() {

  @Test
  fun testStartRepeat() {
    SAE(EventType.REPEAT_START, ea(2))
    RVA("StartRepeat", ea(2))
  }

  @Test
  fun testEndRepeat() {
    SAE(EventType.REPEAT_END, ea(2))
    RVA("EndRepeat", ez(2))
  }

  @Test
  fun testEndRepeatLastBar() {
    RCD(bars = 10)
    SAE(EventType.REPEAT_END, ea(10))
    RVA("EndRepeat", ez(10))
  }
}