package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.dsl.scoreAllCrotchets
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.time.TimeSignature
import org.junit.Test

class BarTest : ScoreTest() {

  @Test
  fun testAddBar() {
    sc.setNewScore(scoreAllCrotchets(4))
    SAE(EventType.BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    assertEqual(5, EG().numBars)
  }

  @Test
  fun testAddBarEventsMove() {
    sc.setNewScore(scoreAllCrotchets(4))
    SAE(EventType.BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    SVNE(EventType.DURATION, eav(3))
    SVE(EventType.DURATION, eav(4))
  }


  @Test
  fun testAddBarSystemEventsMove() {
    SAE(Tempo(crotchet(), 120).toEvent(), ez(2))
    SVE(EventType.TEMPO, ez(2))
    SAE(EventType.BAR, ea(3), paramMapOf(EventParam.NUMBER to 1, EventParam.AFTER to false))
    SVE(EventType.TEMPO, ez(2))
  }

  @Test
  fun testAddBarStaveEventsMove() {
    SAE(EventType.DYNAMIC, ea(3), paramMapOf(EventParam.TYPE to DynamicType.FORTISSIMO,
      EventParam.IS_UP to true))
    SAE(EventType.BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    SVNE(EventType.DYNAMIC, eav(3))
    SVE(EventType.DYNAMIC, eav(4))
  }

  @Test
  fun testAddBarPartEventsMove() {
    SAE(EventType.PEDAL, ea(3), paramMapOf(EventParam.END to ea(4)))
    SAE(EventType.BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    SVNE(EventType.PEDAL, eav(3))
    SVE(EventType.PEDAL, eav(4))
  }

  @Test
  fun testAddBarPartEndEventsMove() {
    SAE(EventType.PEDAL, ea(3), paramMapOf(EventParam.END to ea(4)))
    SAE(EventType.BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    SVP(EventType.PEDAL, EventParam.END, true, eav(5))
  }

  @Test
  fun testAddBarScoreEventsMove() {
    SAE(EventType.KEY_SIGNATURE, ea(3), paramMapOf(EventParam.SHARPS to 2))
    SAE(EventType.BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    SVNE(EventType.KEY_SIGNATURE, ez(3))
    SVE(EventType.KEY_SIGNATURE, ez(4))
  }

  @Test
  fun testAddBarScoreEventsMoveNotBar1() {
    SAE(EventType.BAR, ea(1), paramMapOf(EventParam.NUMBER to 1))
    SVNE(EventType.KEY_SIGNATURE, ez(2))
    SVE(EventType.KEY_SIGNATURE, ez(1))
  }

  @Test
  fun testAddBarScoreEventsMoveBar1() {
    SMV()
    SAE(EventType.BAR, ea(1), paramMapOf(EventParam.NUMBER to 1, EventParam.AFTER to false))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.CHORD, eav(2))
    SVNE(EventType.DURATION, eav(1))
  }


  @Test
  fun testAddBarAppliedAllParts() {
    SCD(instruments = listOf("Violin", "Viola"))
    val bars = EG().numBars
    SAE(EventType.BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    assertEqual(bars + 1, EG().getPart(1)?.getNumBars())
    assertEqual(bars + 1, EG().getPart(2)?.getNumBars())
  }

  @Test
  fun testAddBarAppliedAllStaves() {
    SCD(instruments = listOf("Violin", "Viola"))
    val bars = EG().numBars
    SAE(EventType.BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    assertEqual(bars + 1, EG().getStave(StaveId(1,1))?.bars?.size)
    assertEqual(bars + 1, EG().getStave(StaveId(2,1))?.bars?.size)
  }

  @Test
  fun testAddBarAppliedAllStavesGrandStave() {
    SCDG()
    val bars = EG().numBars
    SAE(EventType.BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    assertEqual(bars + 1, EG().getStave(StaveId(1,1))?.bars?.size)
    assertEqual(bars + 1, EG().getStave(StaveId(1,2))?.bars?.size)
  }

  @Test
  fun testAddBarTsCorrect() {
    SCD(timeSignature = TimeSignature(3, 4))
    SAE(EventType.BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    SMV(eventAddress = eav(3))
    SVVM("C4:R4:R4", eav(3))
  }

  @Test
  fun testDeleteBar() {
    sc.setNewScore(scoreAllCrotchets(4))
    SDE(EventType.BAR, ea(3))
    assertEqual(3, EG().numBars)
  }

  @Test
  fun testDeleteBar1() {
    sc.setNewScore(scoreAllCrotchets(4))
    SDE(EventType.BAR, ea(1))
    assertEqual(3, EG().numBars)
  }

  @Test
  fun testDeleteBarTooMany() {
    sc.setNewScore(scoreAllCrotchets(4))
    SDE(EventType.BAR, ea(3), endAddress = ea(5))
    assertEqual(2, EG().numBars)
  }

  @Test
  fun testDeleteBarEventsMove() {
    sc.setNewScore(scoreAllCrotchets(4))
    SDE(EventType.BAR, ea(3))
    SVNE(EventType.DURATION, eav(4))
    SVE(EventType.DURATION, eav(3))
  }

  @Test
  fun testDeleteBarScoreEventsMove() {
    SAE(EventType.KEY_SIGNATURE, ea(4), paramMapOf(EventParam.SHARPS to 2))
    SDE(EventType.BAR, ea(3))
    SVNE(EventType.KEY_SIGNATURE, ez(4))
    SVE(EventType.KEY_SIGNATURE, ez(3))
  }

  @Test
  fun testDeleteBarStaveEventsMove() {
    SAE(EventType.DYNAMIC, ea(3), paramMapOf(EventParam.TYPE to DynamicType.FORTISSIMO,
      EventParam.IS_UP to true))
    SDE(EventType.BAR, ea(2))
    SVNE(EventType.DYNAMIC, eav(3))
    SVE(EventType.DYNAMIC, eav(2))
  }

  @Test
  fun testDeleteBarScoreEventsMoveNotBar1() {
    SDE(EventType.BAR, ea(1))
    SVE(EventType.KEY_SIGNATURE, ez(1))
  }

  @Test
  fun testDeleteBarScoreEventsGone() {
    SAE(EventType.KEY_SIGNATURE, ea(4), paramMapOf(EventParam.SHARPS to 2))
    SDE(EventType.BAR, ea(4))
    assertEqual(1, EG().getEvents(EventType.KEY_SIGNATURE)?.size)
  }
  
  @Test
  fun testDeleteBarEndAddress() {
    SCD(bars = 32)
    SDE(EventType.BAR, ea(1), ea(4))
    assertEqual(28, EG().numBars)
  }

  @Test
  fun testDeleteLastBar() {
    SCD(bars = 1)
    SDE(EventType.BAR, ea(1))
    assertEqual(1, EG().numBars)
  }

  @Test
  fun testAddBarAfter() {
    SMV()
    SAE(EventType.BAR, ea(1), paramMapOf(EventParam.NUMBER to 1, EventParam.AFTER to true))
    SVE(EventType.DURATION, eav(1))
  }

  @Test
  fun testAddBarOptionsRetained() {
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SAE(EventType.BAR, ea(1), paramMapOf(EventParam.NUMBER to 1, EventParam.AFTER to false))
    assertEqual(true, SGO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT))
  }
}