package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.api.ProgressFunc2
import com.philblandford.kscore.api.noProgress2
import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.AreaDirectory
import com.philblandford.kscore.engine.core.areadirectory.areaDirectory
import com.philblandford.kscore.engine.core.geographyX.GeographyXDirectory
import com.philblandford.kscore.engine.core.geographyX.geographyXDirectory
import com.philblandford.kscore.engine.core.geographyY.GeographyYDirectory
import com.philblandford.kscore.engine.core.geographyY.geographyYDirectory
import com.philblandford.kscore.engine.core.getLayoutDescriptor
import com.philblandford.kscore.engine.core.stave.PartDirectory
import com.philblandford.kscore.engine.core.stave.partDirectory
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.select.SelectState

data class PipeLine(
  val areaDirectory: AreaDirectory,
  val geographyXDirectory: GeographyXDirectory,
  val partDirectory: PartDirectory,
  val geographyYDirectory: GeographyYDirectory
)

sealed class RepUpdate()
data class RepUpdateBar(val start: EventAddress, val end: EventAddress) : RepUpdate()
data class RepUpdateLines(val start: EventAddress) : RepUpdate()
data class RepUpdateXGeog(val start: EventAddress) : RepUpdate()
object RepUpdateNone : RepUpdate()
object RepUpdateFull : RepUpdate()
object RepUpdateOverlay : RepUpdate()

fun scoreToRepresentation(
  scoreQuery: ScoreQuery,
  drawableFactory: DrawableFactory,
  selectState: SelectState = SelectState(),
  layoutDescriptor: LayoutDescriptor = getLayoutDescriptor(
    scoreQuery
  ),
  repCreateType: RepUpdate = RepUpdateFull,
  existing: Representation? = null,
  barChanged: Int? = null,
  progress: ProgressFunc2 = noProgress2
): Representation? {

  existing?.let {
    if (repCreateType is RepUpdateNone) {
      return existing
    }
    if (repCreateType is RepUpdateOverlay) {
      return existing.copy(
        marker = scoreQuery.getParam(EventType.UISTATE, EventParam.MARKER_POSITION, eZero())
      )
    }
  }
  progress("areaDirectory", 15f)
  return drawableFactory.getAreaDirectory(
    scoreQuery,
    repCreateType,
    existing,
    barChanged
  )?.let { ad ->
    if (progress("geographyX", 30f)) return null
    geographyXDirectory(ad, scoreQuery, getAvailable(layoutDescriptor))?.let { gxd ->
      if (progress("partDirectory", 45f)) return null
      drawableFactory.partDirectory(scoreQuery, ad, gxd, layoutDescriptor, progress)?.let { partDir ->
        if (progress("geographyY", 60f)) return null
        geographyYDirectory(partDir, gxd, scoreQuery)?.let { gyd ->
          if (progress("representation", 95f)) return null
          val pipeLine = PipeLine(ad, gxd, partDir, gyd)
          val rep = representation(pipeLine, scoreQuery, layoutDescriptor, drawableFactory)
          if (progress("complete", 100f)) return null
          rep
        }
      }
    }
  }
}


private inline fun postProgress(isCancelled: () -> Boolean, progress: (String, Float) -> Unit) {
  if (isCancelled()) {

  }
}

private fun DrawableFactory.getAreaDirectory(
  scoreQuery: ScoreQuery,
  repUpdate: RepUpdate,
  existing: Representation?,
  barChanged: Int?
): AreaDirectory? {
  return existing?.let {
    when (repUpdate) {
      is RepUpdateFull, is RepUpdateXGeog -> areaDirectory(scoreQuery)
      is RepUpdateLines -> existing.pipeLine.areaDirectory
      is RepUpdateBar -> areaDirectory(scoreQuery, existing.pipeLine.areaDirectory)
      else -> null
    }
  } ?: areaDirectory(scoreQuery)
}

internal fun getAvailable(layoutDescriptor: LayoutDescriptor): Int {
  return layoutDescriptor.pageWidth - layoutDescriptor.leftMargin - layoutDescriptor.rightMargin
}

