package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*



import com.philblandford.kscore.engine.duration.breve
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.time.timeSignature
import core.representation.RepTest


import org.junit.Test

class TimeTest : RepTest() {

  @Test
  fun testTimeSignatureDisplayed() {
    RVA("TimeSignature", ea(1))
  }

  @Test
  fun testTimeSignatureUpbeat() {
    RCD(upbeat = TimeSignature(1, 4))
    val header = getArea("Header", ea(1))!!
    val event = header.area.findByTag("TimeSignature").toList().first().second.event!!
    assertEqual(TimeSignature(4, 4, hidden = false), timeSignature(event))
  }

  @Test
  fun testAddTimeSignatureWithSemibrevesNoteHeads() {
    RCD(TimeSignature(3, 2))
    SMV(duration = breve(1))
    SAE(TimeSignature(4, 4).toEvent(), ez(1))
    val area = getArea("Tadpole", eav(2).copy(id = 1))?.area!!
    assertEqual("tadpole_head_empty", (area.drawable as TestDrawable).tag)
  }
}