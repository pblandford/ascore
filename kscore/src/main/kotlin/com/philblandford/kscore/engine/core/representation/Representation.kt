package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.kscore.log.ksLoge
import com.philblandford.kscore.select.SelectState

data class Representation(
  val pages: List<PageArea>,
  val marker: EventAddress?, val pipeLine: PipeLine, val drawableFactory: DrawableFactory
) {
  private val pagesAsIndexedList = pages.withIndex().toList()

  fun getPageForDrawing(
    page: Int, selectState: SelectState? = null,
    playbackMarker: EventAddress? = null
  ): Area? {
    return pages.toList().getOrNull(page - 1)?.let { (page, geog) ->
      var p = drawableFactory.paintSelection(page, selectState)
      p = drawableFactory.paintSelectedArea(p, geog, selectState)
      p = drawableFactory.paintPlayBackMarker(p, geog, playbackMarker)
      p = paintMarker(p, geog, marker, drawableFactory)
      p
    }
  }

  fun getPageNum(eventAddress: EventAddress): Int {
    return (pagesAsIndexedList.find {
      it.value.geography.startBar <= eventAddress.barNum
              && it.value.geography.endBar >= eventAddress.barNum
    }?.index ?: 0) + 1
  }

  fun getEndBarForSegment(eventAddress: EventAddress): Int {
    val numBars = getArea("Multibar", eventAddress)?.let { multi ->
      (multi.second.extra as? Int)
    } ?: 1
    return eventAddress.barNum + (numBars - 1)
  }

  fun getPage(page: Int): PageArea? {
    return pagesAsIndexedList.getOrNull(page - 1)?.value
  }

  override fun toString(): String {
    return "Representation ${pages.size} pages ${hashCode()}"
  }
}

internal fun representation(
  pipeLine: PipeLine, scoreQuery: ScoreQuery, layoutDescriptor: LayoutDescriptor,
  drawableFactory: DrawableFactory
): Representation {
  val pages = drawableFactory.createPages(
    pipeLine.partDirectory, scoreQuery, pipeLine.geographyYDirectory,
    layoutDescriptor
  )

  return Representation(
    pages,
    scoreQuery.getParam(EventType.UISTATE, EventParam.MARKER_POSITION),
    pipeLine,
    drawableFactory
  )
}



