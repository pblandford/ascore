package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.AreaDirectory
import com.philblandford.kscore.engine.core.areadirectory.areaDirectory
import com.philblandford.kscore.engine.core.geographyX.GeographyXDirectory
import com.philblandford.kscore.engine.core.geographyX.geographyXDirectoryDiff
import com.philblandford.kscore.engine.core.geographyY.geographyYDirectory
import com.philblandford.kscore.engine.core.getLayoutDescriptor
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.stave.update
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.kscore.engine.update.ScoreDiff
import com.philblandford.kscore.engine.update.diff
import com.philblandford.kscore.log.ksLoge

private val noopDiff = ScoreDiff(listOf(), listOf(), false, false, false)

internal fun Representation.update(
  oldScore: Score,
  score: Score,
  layoutDescriptor: LayoutDescriptor = getLayoutDescriptor(
    score
  )
): Representation {

  /* get a descriptor of what's changed in the score */
  val scoreDiff = oldScore.diff(score)

  ksLoge("RU $scoreDiff")

  /* nothing has changed except maybe top-layer stuff like the marker position */
  if (scoreDiff == noopDiff) {
    return Representation(
      pages,
      score.getParam(EventType.UISTATE, EventParam.MARKER_POSITION), pipeLine, drawableFactory
    )
  }

  /* too much has changed for an incremental update -  rebuild the whole thing */
  if (scoreDiff.recreate) {
    return scoreToRepresentation(score, drawableFactory) ?: this
  }

  /* create a new area directory */
  val ad = drawableFactory.refreshAreas(pipeLine.areaDirectory, scoreDiff, score)

  /* create all the parts and systems */
  return if (scoreDiff.createParts) {
    geographyXDirectoryDiff(
      ad,
      score,
      getAvailable(layoutDescriptor),
      pipeLine.geographyXDirectory,
      scoreDiff.changedBars.map { it.barNum }.distinct()
    ).let { gxd ->
      val geogChanged = getXGeogBarsChanged(pipeLine.geographyXDirectory, gxd)

      val partDir = pipeLine.partDirectory.update(
        score,
        ad,
        gxd,
        layoutDescriptor,
        false,
        scoreDiff.changedBars.map { it.barNum }.plus(geogChanged).plus(scoreDiff.changedLines),
        drawableFactory
      )
      geographyYDirectory(partDir, gxd, score)?.let { gyd ->
        val pipeLine = PipeLine(ad, gxd, partDir, gyd)
        val rep =
          representation(
            pipeLine,
            score,
            layoutDescriptor,
            drawableFactory
          )
        rep
      }
    } ?: this
  } else {
    this
  }
}


private fun getXGeogBarsChanged(old: GeographyXDirectory, new: GeographyXDirectory): Iterable<Int> {
  val unchanged = old.sxGeographies.intersect(new.sxGeographies.toSet())
  val oldDiff = old.sxGeographies.minus(unchanged)
  val newDiff = new.sxGeographies.minus(unchanged)
  return oldDiff.plus(newDiff).map { it.startBar }.distinct()
}


internal fun DrawableFactory.refreshAreas(
  areaDir: AreaDirectory, scoreDiff: ScoreDiff,
  scoreQuery: ScoreQuery
): AreaDirectory {
  return areaDirectory(scoreQuery, areaDir, scoreDiff.changedBars, scoreDiff.createHeaders)
    ?: areaDir
}
