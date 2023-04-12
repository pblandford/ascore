package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*

import org.junit.Test

class BarLineTest : ScoreTest() {

  @Test
  fun testAddBarLine() {
    SAE(EventType.BARLINE, ez(2), paramMapOf(EventParam.TYPE to BarLineType.DOUBLE))
    SVP(EventType.BARLINE, EventParam.TYPE, BarLineType.DOUBLE, ez(2))
  }

  @Test
  fun testAddRepeatStart() {
    SAE(EventType.REPEAT_START, ez(2))
    SVE(EventType.REPEAT_START, ez(2))
  }

  @Test
  fun testAddRepeatEnd() {
    SAE(EventType.REPEAT_END, ez(2))
    SVE(EventType.REPEAT_END, ez(2))
  }

  @Test
  fun testAddNormalRemovesDoubleBar() {
    SAE(EventType.BARLINE, ez(2),  paramMapOf(EventParam.TYPE to BarLineType.DOUBLE))
    SAE(EventType.BARLINE, ez(2), paramMapOf(EventParam.TYPE to BarLineType.NORMAL))
    SVNE(EventType.BARLINE, ez(2))
  }

  @Test
  fun testAddNormalRemovesRepeatEnd() {
    SAE(EventType.REPEAT_END, ez(2))
    SAE(EventType.BARLINE, ez(2), paramMapOf(EventParam.TYPE to BarLineType.NORMAL))
    SVNE(EventType.REPEAT_END, ez(2))
  }

  @Test
  fun testAddNormalRemovesRepeatStartNextBar() {
    SAE(EventType.REPEAT_START, ez(2))
    SAE(EventType.BARLINE, ez(1), paramMapOf(EventParam.TYPE to BarLineType.NORMAL))
    SVNE(EventType.REPEAT_START, ez(2))
  }

  @Test
  fun testAddRepeatStartAsBarLineSubtype() {
    SAE(EventType.BARLINE, ez(2), paramMapOf(EventParam.TYPE to BarLineType.START_REPEAT))
    SVE(EventType.REPEAT_START, ez(2))
  }

  @Test
  fun testAddRepeatEndAsBarLineSubtype() {
    SAE(EventType.BARLINE, ez(2), paramMapOf(EventParam.TYPE to BarLineType.END_REPEAT))
    SVE(EventType.REPEAT_END, ez(2))
  }

  @Test
  fun testAddRepeatEndShowsEndParam() {
    SAE(EventType.REPEAT_END, ez(2))
    SVP(EventType.BARLINE, EventParam.TYPE, BarLineType.END_REPEAT, ez(2))
  }
}