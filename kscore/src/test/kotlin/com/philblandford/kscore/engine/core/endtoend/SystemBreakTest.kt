package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import assertNotEqual
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class SystemBreakTest : RepTest() {

  @Test
  fun testSystemBreakApplied() {
    SAE(EventType.BREAK, ez(3), paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    assertEqual(4, getStaveBar(1))
  }

  @Test
  fun testSystemBreakNotAppliedSelectedPart() {
    RCD(instruments = listOf("Violin", "Viola"))
    SAE(EventType.BREAK, ez(3), paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    sc.setSelectedPart(1)
    assert(getStaveBar(1) != 4)
  }

  @Test
  fun testSystemBreakAppliedSelectedPart() {
    RCD(instruments = listOf("Violin", "Viola"))
    sc.setSelectedPart(1)
    SAE(EventType.BREAK, ea(3), paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    assertEqual(4, getStaveBar(1))
  }

  @Test
  fun testSystemBreakSymbol() {
    SAE(EventType.BREAK, ez(3), paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    RVA("SystemBreak", ez(3))
  }

  @Test
  fun testSystemBreakSymbolNotSecondPart() {
    RCD(instruments = listOf("Violin", "Viola"))
    SAE(EventType.BREAK, ez(3), paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    assertEqual(1, getAreas("SystemBreak").size)
  }

  @Test
  fun testSystemBreakSymbolForPartNotShownInFull() {
    RCD(instruments = listOf("Violin", "Viola"))
    sc.setSelectedPart(1)
    SAE(EventType.BREAK, ea(3), paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    sc.setSelectedPart(0)
    RVNA("SystemBreak", ez(3))
  }

  @Test
  fun testSystemBreakUnApplied() {
    val old = getStaveBar(1)
    SAE(EventType.BREAK, ez(3), paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    SDE(EventType.BREAK, ez(3))
    assertEqual(old, getStaveBar(1))
  }

  @Test
  fun testSystemBreakUnAppliedSelectedPart() {
    RCD(instruments = listOf("Violin", "Viola"))
    sc.setSelectedPart(1)
    val old = getStaveBar(1)
    SAE(EventType.BREAK, ea(3), paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    SDE(EventType.BREAK, ez(3))
    assertEqual(old, getStaveBar(1))
  }
}