package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.paramMapOf
import org.junit.Test

class HardStartTest : ScoreTest() {

  @Test
  fun testAddHardStart() {
    SAE(EventType.DYNAMIC, ea(1))
    SSP(EventType.DYNAMIC, EventParam.HARD_START, Coord(5,10), ea(1))
    SVP(EventType.DYNAMIC, EventParam.HARD_START, Coord(5,10), ea(1))
  }

  @Test
  fun testAddHardStartToExisting() {
    SAE(EventType.DYNAMIC, ea(1))
    SSP(EventType.DYNAMIC, EventParam.HARD_START, Coord(5,10), ea(1))
    SSP(EventType.DYNAMIC, EventParam.HARD_START, Coord(5,10), ea(1))
    SVP(EventType.DYNAMIC, EventParam.HARD_START, Coord(10,20), ea(1))
  }

  @Test
  fun testAddHardStartToMeta() {
    SAE(EventType.TITLE, ea(1), paramMapOf(EventParam.TEXT to "Hello"))
    SSP(EventType.TITLE, EventParam.HARD_START, Coord(5,10), ea(1))
    SVP(EventType.TITLE, EventParam.HARD_START, Coord(5,10), ea(1))
  }

  @Test
  fun testAddHardStartToMetaExisting() {
    SAE(EventType.TITLE, ea(1), paramMapOf(EventParam.TEXT to "Hello"))
    SSP(EventType.TITLE, EventParam.HARD_START, Coord(5,10), ea(1))
    SSP(EventType.TITLE, EventParam.HARD_START, Coord(5,10), ea(1))
    SVP(EventType.TITLE, EventParam.HARD_START, Coord(10,20), ea(1))
  }

  @Test
  fun testAddHardStartToLineExisting() {
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to 1), ea(2))
    SSP(EventType.OCTAVE, EventParam.HARD_START, Coord(5,10), ea(1))
    SSP(EventType.OCTAVE, EventParam.HARD_START, Coord(5,10), ea(1))
    SVP(EventType.OCTAVE, EventParam.HARD_START, Coord(10,20), ea(1))
  }

  @Test
  fun testAddHardStartToTextExisting() {
    SAE(EventType.TEMPO_TEXT, ea(1), paramMapOf(EventParam.TEXT to "Hello"))
    SSP(EventType.TEMPO_TEXT, EventParam.HARD_START, Coord(5,10), ea(1))
    SSP(EventType.TEMPO_TEXT, EventParam.HARD_START, Coord(5,10), ea(1))
    SVP(EventType.TEMPO_TEXT, EventParam.HARD_START, Coord(10,20), ea(1))
  }

  @Test
  fun testAddHardStartToExpressionExisting() {
    SAE(EventType.EXPRESSION_TEXT, ea(1), paramMapOf(EventParam.TEXT to "Hello"))
    SSP(EventType.EXPRESSION_TEXT, EventParam.HARD_START, Coord(5,10), ea(1))
    SSP(EventType.EXPRESSION_TEXT, EventParam.HARD_START, Coord(5,10), ea(1))
    SVP(EventType.EXPRESSION_TEXT, EventParam.HARD_START, Coord(10,20), ea(1))
  }
}