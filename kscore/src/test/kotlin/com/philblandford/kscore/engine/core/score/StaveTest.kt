package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.dsl.dslStave
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventType
import org.junit.Test

internal class StaveTest {

  @Test
  fun testGetBar() {
    val stave = dslStave({ TimeSignature(4, 4) }) {
      repeat(10) {
        bar { }
      }
    }
    val bar = stave.getBar(2)
    assert(bar != null)
  }


}