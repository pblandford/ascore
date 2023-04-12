package com.philblandford.kscore.engine.core.endtoend



import com.philblandford.kscore.engine.duration.quaver
import com.philblandford.kscore.engine.duration.times
import com.philblandford.kscore.engine.types.*
import core.representation.RepTest
import org.junit.Test

class RepeatBarTest : RepTest() {

  @Test
  fun testRepeatBarAdded() {
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    RVA("RepeatBar", ea(2))
  }

  @Test
  fun testRepeatBarAddedTwoBars() {
    SAE(EventType.REPEAT_BAR, ea(3), paramMapOf(EventParam.NUMBER to 2))
    RVA("RepeatBar", ea(3))
  }

  @Test
  fun testRepeatBarAddedBeamsRemoved() {
    repeat(8) {
      SMV(eventAddress = eav(2, quaver() * it))
    }
    RVNA("Beam", eav(2))

    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    RVNA("Beam", eav(2))
  }
}