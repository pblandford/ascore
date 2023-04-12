package com.philblandford.kscore.engine.core.score.scorequery

import TestInstrumentGetter
import assertEqual


import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.dsl.scoreAllCrotchets
import com.philblandford.kscore.engine.duration.*

import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.eag
import grace
import org.junit.Test
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.types.eagv
import com.philblandford.kscore.engine.types.eav

class StaveSegmentTest : ScoreTest() {

  @Test
  fun testGetPreviousStaveSegment() {
    val score = scoreAllCrotchets(4)
    assertEqual(ea(1, crotchet()), score.getPreviousStaveSegment(ea(1, minim())))
  }

  @Test
  fun testGetPreviousStaveSegmentAcrossBar() {
    val score = scoreAllCrotchets(4)
    assertEqual(ea(1, minim(1)), score.getPreviousStaveSegment(ea(2)))
  }

  @Test
  fun testGetPreviousStaveSegmentNormalAfterGrace() {
    SMV()
    grace()
    assertEqual(eag(1), EG().getPreviousStaveSegment(ea(1)))
  }

  @Test
  fun testGetNextStaveSegment() {
    val score = scoreAllCrotchets(4)
    assertEqual(ea(1, minim()), score.getNextStaveSegment(ea(1, crotchet())))
  }

  @Test
  fun testGetNextStaveSegmentAcrossBar() {
    val score = scoreAllCrotchets(4)
    assertEqual(ea(2), score.getNextStaveSegment(ea(1, minim(1))))
  }

  @Test
  fun testGetNextStaveSegmentEmptyBars() {
    val score =
      Score.create(TestInstrumentGetter(), 32)
    assertEqual(ea(2), score.getNextStaveSegment(ea(1)))
  }

  @Test
  fun testGetNextStaveSegmentGrace() {
    SMV()
    grace()
    grace()
    assertEqual(eag(1, dZero(), semiquaver()), EG().getNextStaveSegment(eag(1)))
  }

  @Test
  fun testGetNextStaveSegmentGraceToNormal() {
    SMV()
    grace()
    assertEqual(ea(1), EG().getNextStaveSegment(eag(1)))
  }

  @Test
  fun testGetNextStaveSegmentGraceToEmpty() {
    grace()
    assertEqual(ea(1), EG().getNextStaveSegment(eag(1)))
  }

  @Test
  fun testGetNextStaveSegmentTuplet() {
    SAE(tuplet(dZero(), 3,8).toEvent())
    assertEqual((ea(1, Offset(1,12))), EG().getNextStaveSegment(ea(1)))
  }

  @Test
  fun testGetFloorStaveSegment() {
    SMV()
    assertEqual(ea(1), EG().getFloorStaveSegment(ea(1, quaver())))
  }

  @Test
  fun testGetFloorStaveSegmentSameSegment() {
    SMV()
    assertEqual(ea(1), EG().getFloorStaveSegment(ea(1)))
  }

  @Test
  fun testGetFloorStaveSegmentEmptyBar() {
    SMV()
    assertEqual(ea(2), EG().getFloorStaveSegment(ea(2, crotchet())))
  }

  @Test
  fun testGetFloorStaveSegmentGrace() {
    grace()
    assertEqual(eag(1), EG().getFloorStaveSegment(eag(1, dZero(), quaver())))
  }

  @Test
  fun testGetFloorStaveSegmentGraceSame() {
    grace()
    assertEqual(eag(1), EG().getFloorStaveSegment(eag(1)))
  }

  @Test
  fun testGetNextVoiceSegment() {
    SMV()
    assertEqual(eav(1, crotchet()), EG().getNextVoiceSegment(eav(1)))
  }

  @Test
  fun testGetNextVoiceSegmentNextStaveSegmentIsCloser() {
    SMV(eventAddress = eav(1, dZero(), 2))
    SMV(duration = quaver(), eventAddress = eav(1, dZero()))
    assertEqual(eav(1, quaver()), EG().getNextVoiceSegment(eav(1)))
  }

  @Test
  fun testGetPreviousVoiceSegment() {
    SMV()
    assertEqual(eav(1), EG().getPreviousVoiceSegment(eav(1, crotchet())))
  }

  @Test
  fun testGetPreviousVoicePreviousBar() {
    SMV()
    SMV(eventAddress = eav(2))
    assertEqual(eav(1, minim()), EG().getPreviousVoiceSegment(eav(2)))
  }

  @Test
  fun testGetPreviousVoicePreviousBarEmpty() {
    SMV(eventAddress = eav(2))
    assertEqual(eav(1), EG().getPreviousVoiceSegment(eav(2)))
  }

  @Test
  fun testGetNextVoiceSegmentNonGraceToGrace() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    grace(mainOffset = crotchet())
    assertEqual(eagv(1, crotchet()), EG().getNextVoiceSegment(eav(1)))
  }

}