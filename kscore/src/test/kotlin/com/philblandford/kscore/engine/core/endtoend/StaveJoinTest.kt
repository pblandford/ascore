package com.philblandford.kscore.engine.core.endtoend

import assertNotEqual
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.dZero

import com.philblandford.kscore.engine.core.representation.RepTest
import com.philblandford.kscore.engine.core.representation.getArea
import org.junit.Test

class StaveJoinTest : RepTest() {

  @Test
  fun testStaveJoin() {
    RCD(instruments = listOf("Violin", "Viola"))
    SAE(
      EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(
        EventParam.TYPE to StaveJoinType.BRACKET,
        EventParam.END to eZero().copy(staveId = StaveId(2,0)))
    )
    RVA("StaveJoin", eas(0, dZero(), StaveId(1,0)))
  }

  @Test
  fun testStaveJoinGrand() {
    RCD(instruments = listOf("Violin", "Viola"))
    SAE(
      EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(
        EventParam.TYPE to StaveJoinType.GRAND,
        EventParam.END to eZero().copy(staveId = StaveId(2,0)))
    )
    RVA("StaveJoin", eas(0, dZero(), StaveId(1,0)))
  }

  @Test
  fun testPianoStaveCreated() {
    RCD(instruments = listOf("Piano"))
    RVA("StaveJoin", eas(0, dZero(), StaveId(1,0)))
  }

  @Test
  fun testPianoStaveBeforeStave() {
    RCD(instruments = listOf("Piano"))
    assert(isLeft("StaveJoin", eas(0, dZero(), StaveId(1,0)),
      "SystemBarLine", ez(1))!!)
  }

  @Test
  fun testDeleteStaveJoin() {
    RCD(instruments = listOf("Violin", "Viola"))
    SAE(
      EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(
        EventParam.TYPE to StaveJoinType.BRACKET,
        EventParam.END to eZero().copy(staveId = StaveId(2,0)))
    )
    SDE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)))
    RVNA("StaveJoin", eas(0, dZero(), StaveId(1,0)))
  }

  @Test
  fun testStaveJoinAddsToPreheaderWidth() {
    RCD(instruments = listOf("Violin", "Viola"))
    val beforeWidth = REP().getArea("System", ez(1))!!.second.width
    SAE(
      EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(
        EventParam.TYPE to StaveJoinType.BRACKET,
        EventParam.END to eZero().copy(staveId = StaveId(2,0)))
    )
    val afterWidth= REP().getArea("PreHeader", ez(1).copy(staveId = StaveId(1,0)))!!.second.width
    assertNotEqual(afterWidth, beforeWidth)
  }

}