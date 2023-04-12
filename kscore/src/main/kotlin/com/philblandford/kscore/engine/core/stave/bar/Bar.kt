package com.philblandford.kscore.engine.core.stave.bar

import com.philblandford.kscore.engine.core.ResolvedBarGeography
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarEndAreaPair
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarStartAreaPair
import com.philblandford.kscore.engine.core.areadirectory.segment.SegmentArea
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.RepeatBarType


internal fun DrawableFactory.createBar(
  segments: Map<Offset, SegmentArea>,
  barGeography: ResolvedBarGeography,
  barStartArea: BarStartAreaPair,
  barEndArea: BarEndAreaPair,
  repeatBarType: RepeatBarType?,
  eventAddress: EventAddress,
  emptyVoiceMaps: Iterable<EventAddress>
): Area {

  val placeHolderActual = segments.toList().partition { it.second.placeHolder }
  val placeHolderSegments = placeHolderActual.first.toMap()
  val actualSegments = placeHolderActual.second.toMap()

  var area = if (repeatBarType != null) {
    createRepeatBar(
      eventAddress,
      barGeography,
      repeatBarType
      )
  } else if (barGeography.numBars > 1) {
    createMultiBar(
      barGeography,
      eventAddress
    )
  } else if (actualSegments.isEmpty()) {
    createEmptyBar(
      barGeography,
      true,
      eventAddress.copy(voice = 1),
      placeHolderSegments.keys
    )
  } else {
    createNonEmptyBar(
      barGeography,
      actualSegments,
      eventAddress,
      emptyVoiceMaps
    )
  }
  area = addBarStartEnd(
    area,
    barStartArea,
    barEndArea,
    barGeography,
    eventAddress
  )
  return area
}

