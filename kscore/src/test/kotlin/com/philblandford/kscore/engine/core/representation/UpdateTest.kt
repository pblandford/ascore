package com.philblandford.kscore.engine.core.representation

import assertEqual
import com.philblandford.kscore.engine.types.StaveId
import com.philblandford.kscore.engine.types.ea

import com.philblandford.kscore.engine.core.score.ScoreContainer
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.update.diff
import core.representation.RepTest
import org.junit.Test

class UpdateTest : RepTest() {

  @Test
  fun testUpdateAreaDirectory() {
    val oldScore = SCORE()
    val rep = REP()
    SMV()
    val newScore = SCORE()
    val diff = SCORE().diff(oldScore)
    val ad = drawableFactory.refreshAreas(rep.pipeLine.areaDirectory, diff, newScore)
    assertEqual(setOf(ea(1), ea(1, crotchet()), ea(1, minim())).toSet(),
      ad.getSegmentsForStave(StaveId(1,1)).keys.toSet())
  }

  @Test
  fun testUpdateAreaGeogsUpdated() {
    val oldScore = SCORE()
    val rep = REP()
    SMV()
    val newScore = SCORE()
    val diff = newScore.diff(oldScore)
    val ad = drawableFactory.refreshAreas(rep.pipeLine.areaDirectory, diff, newScore)
    assertEqual(setOf(ea(1), ea(1, crotchet()), ea(1, minim())).toSet(),
      ad.getSegmentGeogsForColumn(1)?.get(StaveId(1,1))?.keys?.toSet())
  }

  @Test
  fun testUpdateGeographyXDirectory() {
    val oldScore = SCORE()
    val rep = REP()
    SMV()
    val newScore = SCORE()
    val diff = SCORE().diff(oldScore)
    val ad = drawableFactory.refreshAreas(rep.pipeLine.areaDirectory, diff, newScore)

  }


}