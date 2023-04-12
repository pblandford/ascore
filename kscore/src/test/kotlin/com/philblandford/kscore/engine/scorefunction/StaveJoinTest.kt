package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.dsl.scoreMultiInstruments
import org.junit.Test

class StaveJoinTest : ScoreTest() {

  @Test
  fun testAddStaveJoin() {
    sc.setNewScore(scoreMultiInstruments(2,2))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET,
        EventParam.END to eZero().copy(staveId = StaveId(2,0))))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 2, ez(0).copy(staveId = StaveId(1,0)))
  }

  @Test
  fun testAddStaveJoinOverwritesExisting() {
    sc.setNewScore(scoreMultiInstruments(3,2))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET, EventParam.END to eZero().copy(staveId = StaveId(1,0))))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 1, ez(0).copy(staveId = StaveId(1,0)))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET, EventParam.END to eZero().copy(staveId = StaveId(3,0))))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 3, ez(0).copy(staveId = StaveId(1,0)))
  }

  @Test
  fun testAddStaveJoinOverlap() {
    sc.setNewScore(scoreMultiInstruments(3,2))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET, EventParam.END to eZero().copy(staveId = StaveId(2,0))))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 2, ez(0).copy(staveId = StaveId(1,0)))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(2,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET, EventParam.END to eZero().copy(staveId = StaveId(3,0))))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 1, ez(0).copy(staveId = StaveId(1,0)))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 2, ez(0).copy(staveId = StaveId(2,0)))
  }

  @Test
  fun testAddStaveJoinOverlapDown() {
    sc.setNewScore(scoreMultiInstruments(3,2))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(2,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET, EventParam.END to eZero().copy(staveId = StaveId(3,0))))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 2, ez(0).copy(staveId = StaveId(2,0)))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET, EventParam.END to eZero().copy(staveId = StaveId(2,0))))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 2, ez(0).copy(staveId = StaveId(1,0)))
    SVNE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(2,0)))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 1, ez(0).copy(staveId = StaveId(3,0)))
  }

  @Test
  fun testAddStaveJoinBeneathExisting() {
    sc.setNewScore(scoreMultiInstruments(4,2))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET,
        EventParam.END to eZero().copy(staveId = StaveId(2,0))))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(3,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET,
        EventParam.END to eZero().copy(staveId = StaveId(4,0))))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 2, ez(0).copy(staveId = StaveId(1,0)))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 2, ez(0).copy(staveId = StaveId(3,0)))
  }

  @Test
  fun testAddStaveJoinGrandStave() {
    SCD(instruments = listOf("Piano"))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET,
        EventParam.END to eZero().copy(staveId = StaveId(1,2))))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 1, ez(0).copy(staveId = StaveId(1,0)))
    SVNE(EventType.STAVE_JOIN,  ez(0).copy(staveId = StaveId(2,0)))
  }

  @Test
  fun testAddStaveJoinAboveGrandStave() {
    SCD(instruments = listOf("Violin", "Violin", "Piano"))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET,
        EventParam.END to eZero().copy(staveId = StaveId(2,0))))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 2, ez(0).copy(staveId = StaveId(1,0)))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 1, ez(0).copy(staveId = StaveId(3,0)))
  }

  @Test
  fun testDeleteStaveJoin() {
    sc.setNewScore(scoreMultiInstruments(2,2))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(3,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET,
        EventParam.END to eZero().copy(staveId = StaveId(3,0))))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET,
        EventParam.END to eZero().copy(staveId = StaveId(2,0))))
    SDE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)))
    SVNE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(1,0)))
  }
  
  @Test
  fun testAddStaveJoinReverseAddresses() {
    sc.setNewScore(scoreMultiInstruments(2,2))
    SAE(EventType.STAVE_JOIN, ez(0).copy(staveId = StaveId(2,0)),
      paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET,
        EventParam.END to eZero().copy(staveId = StaveId(1,0))))
    SVP(EventType.STAVE_JOIN, EventParam.NUMBER, 2, ez(0).copy(staveId = StaveId(1,0)))
  }
}