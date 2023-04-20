package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.pitch.KeySignature

import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class MultiBarTest : RepTest() {

  @Test
  fun testMultiBar() {
    val numBars = EG().numBars
    SSO(EventParam.OPTION_SHOW_MULTI_BARS, true)
    RVA("Number-$numBars", ea(1))
  }

  @Test
  fun testMultiBarRightOfKS() {
    val numBars = EG().numBars
    SAE(KeySignature(1).toEvent(), ez(2))
    SSO(EventParam.OPTION_SHOW_MULTI_BARS, true)
    assert(isLeft("KeySignature", ea(2), "Multibar", ea(2))!!)
  }

  @Test
  fun testMultiBarWithDoubleBar() {
    SAE(EventType.BARLINE, ez(6), paramMapOf(EventParam.TYPE to BarLineType.DOUBLE))
    SSO(EventParam.OPTION_SHOW_MULTI_BARS, true)
    RVA("Number-6", ea(1))
  }

  @Test
  fun testMultiBarPageBreak() {
    SAE(EventType.BREAK, ez(6))
    SSO(EventParam.OPTION_SHOW_MULTI_BARS, true)
    RVA("Number-6", ea(1))
  }

  @Test
  fun testMultiBarDalSegno() {
    SAE(EventType.NAVIGATION, ez(6), paramMapOf(EventParam.TYPE to NavigationType.DAL_SEGNO))
    SSO(EventParam.OPTION_SHOW_MULTI_BARS, true)
    RVA("Number-6", ea(1))
  }

  @Test
  fun testMultiBarDalSegnoDrawn() {
    SAE(EventType.NAVIGATION, ez(6), paramMapOf(EventParam.TYPE to NavigationType.DAL_SEGNO))
    SSO(EventParam.OPTION_SHOW_MULTI_BARS, true)
    RVA("Navigation", ez(6))
  }

  @Test
  fun testMultiBarSegnoDrawn() {
    SAE(EventType.NAVIGATION, ez(6), paramMapOf(EventParam.TYPE to NavigationType.SEGNO))
    SSO(EventParam.OPTION_SHOW_MULTI_BARS, true)
    RVA("Navigation", ez(6))
  }

  @Test
  fun testMultiBarWithRepeatBars() {
    RCD(instruments = listOf("Violin", "Viola"), bars = 10)
    repeat(5) {
      SAE(EventType.REPEAT_BAR, eas(it+1, 2, 1), paramMapOf(EventParam.NUMBER to 1))
    }
    SSO(EventParam.OPTION_SHOW_MULTI_BARS, true)
    RVNA("Number-10", ea(1))
    RVA("Number-5", ea(6))
  }

  @Test
  fun testMultiBarWithTwoBarRepeatBars() {
    RCD(instruments = listOf("Violin", "Viola"), bars = 10)
    repeat(3) {
      SAE(EventType.REPEAT_BAR, eas(it*2 + 1, 2, 1), paramMapOf(EventParam.NUMBER to 2))
    }
    SSO(EventParam.OPTION_SHOW_MULTI_BARS, true)
    RVNA("Number-10", ea(1))
    RVA("Number-4", ea(7))
  }
}