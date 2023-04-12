package com.philblandford.kscore.engine.core.stave.bar

import com.philblandford.kscore.engine.core.ResolvedBarGeography
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.segment.SegmentArea
import com.philblandford.kscore.engine.core.representation.STAVE_HEIGHT
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dMax
import com.philblandford.kscore.engine.types.EventAddress

private const val inputAreaCacheId = "InputArea"
internal fun DrawableFactory.addInputAreas(
  segments: Map<Duration, SegmentArea>,
  barGeography: ResolvedBarGeography,
  barArea: Area, eventAddress: EventAddress
): Area {
  return if (segments.isEmpty()) {
    val inputArea = Area(
      barGeography.segmentWidth, STAVE_HEIGHT, tag = "SegmentInput",
      addressRequirement = AddressRequirement.SEGMENT
    )
    val start = barGeography.barStartGeography.width
    barArea.addArea(inputArea, Coord(start, 0), eventAddress)
  } else {
    var areaCopy = barArea
    val segmentList = segments.toList().sortedBy { it.first }
    val positions = barGeography.original.slicePositions.map { it.key.offset to it.value }.toMap()
    (segmentList.indices).forEach {
      val offset = segmentList[it].first
      val start = (positions[offset]?.xMargin ?: 0) + barGeography.barStartGeography.width
      val nextOffset = segmentList.getOrNull(it + 1)?.first ?: dMax()
      val width = (positions[nextOffset]?.start
        ?: barGeography.segmentWidth) + barGeography.barStartGeography.width - start
      val inputArea = Area(
        width,
        STAVE_HEIGHT,
        tag = "SegmentInput",
        addressRequirement = AddressRequirement.SEGMENT
      )
      areaCopy = areaCopy.addArea(
        inputArea,
        Coord(start, 0),
        eventAddress.copy(offset = offset)
      )
    }
    areaCopy
  }
}
