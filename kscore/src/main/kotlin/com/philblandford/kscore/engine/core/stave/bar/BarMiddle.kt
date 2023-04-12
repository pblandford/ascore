package com.philblandford.kscore.engine.core.stave.bar

import com.philblandford.kscore.engine.core.ResolvedBarGeography
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.barstartend.placeholderBarLine
import com.philblandford.kscore.engine.core.areadirectory.segment.SegmentArea
import com.philblandford.kscore.engine.core.areadirectory.segment.repeatBarArea
import com.philblandford.kscore.engine.core.areadirectory.segment.restArea
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.core.stave.multiBarArea
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.dMax
import com.philblandford.kscore.engine.duration.semibreve
import com.philblandford.kscore.engine.types.*
import kotlin.math.max
import kotlin.math.min

internal fun DrawableFactory.createNonEmptyBar(
  barGeography: ResolvedBarGeography,
  segments: Map<Duration, SegmentArea>,
  eventAddress: EventAddress,
  emptyVoiceMaps: Iterable<EventAddress>
): Area {
  var area =
    Area(tag = "Bar", addressRequirement = AddressRequirement.BAR, event = Event(EventType.BAR))
  val restShifts = getRestShifts(segments)

  val terminated = segments.toList().plus(
    dMax() to (null as SegmentArea?)
  ).sortedBy { it.first }

  terminated.windowed(2) { sublist ->
    val segmentPair = sublist.first()
    val nextSegment = sublist.last()
    segmentPair.second?.let { segment ->
      barGeography.original.slicePositions[hz(segmentPair.first)]?.let { slice ->
        val endSlice = barGeography.original.slicePositions[hz(nextSegment.first)]?.start
          ?: barGeography.segmentWidth
        val xPos = slice.xMargin + barGeography.barStartGeography.width
        val shifted =
          adjustRests(segment, restShifts)
        val width = endSlice - slice.start
        val yMargin = max(STAVE_HEIGHT/2, shifted.base.yMargin)
        val yBelowStave = max(STAVE_HEIGHT/2, shifted.base.height - yMargin - STAVE_HEIGHT)
        val base =
          shifted.base.copy(width = width, height = STAVE_HEIGHT + yMargin + yBelowStave,
            ignoreX = shifted.base.xMargin,
            yMargin = yMargin,
            addressRequirement = AddressRequirement.SEGMENT)
        area = area.addArea(base, Coord(xPos, 0), eventAddress.copy(offset = segmentPair.first))
      }
    }
  }
  emptyVoiceMaps.forEach {
    var pos = (area.getTopForRange(
      0,
      area.width
    ) - BLOCK_HEIGHT * 5) / (BLOCK_HEIGHT * 2) * (BLOCK_HEIGHT * 2)
    pos = if (it.voice == 1) min(REST_LINE_V1, pos) else max(REST_LINE_V2, pos)
    area = addWholeBarRest(
      area,
      barGeography,
      it,
      pos
    )
  }
  return area
}

internal fun DrawableFactory.addWholeBarRest(
  barArea: Area, resolvedBarGeography: ResolvedBarGeography,
  eventAddress: EventAddress, pos: Int = 0
): Area {
  val mid = resolvedBarGeography.barStartGeography.width + resolvedBarGeography.segmentWidth / 2
  val rest = restArea(
    Event(
      EventType.DURATION, paramMapOf(
        EventParam.DURATION to semibreve(),
        EventParam.TYPE to DurationType.REST
      )
    )
  )?.base ?: Area()
  return barArea.addArea(rest, Coord(mid - rest.width / 2, pos), eventAddress)
}

internal fun DrawableFactory.createEmptyBar(
  resolvedBarGeography: ResolvedBarGeography,
  wholeBarRest: Boolean,
  eventAddress: EventAddress,
  placeHolderSegments: Iterable<Offset>
): Area {
  val barMiddle = Area(
    width = resolvedBarGeography.segmentWidth,
    height = STAVE_HEIGHT,
    tag = "Segment",
    addressRequirement = AddressRequirement.SEGMENT,
    event = Event(EventType.BAR)
  )
  val barArea = Area(tag = "Bar").addArea(
    barMiddle,
    Coord(resolvedBarGeography.barStartGeography.width),
    eventAddress = eventAddress.voiceIdless(),
  )
  val area = if (wholeBarRest) addWholeBarRest(
    barArea,
    resolvedBarGeography,
    eventAddress
  ) else {
    barArea
  }
  return addPlaceHolders(area, placeHolderSegments, resolvedBarGeography, eventAddress)
}

private fun DrawableFactory.addPlaceHolders(
  area: Area, placeHolderSegments: Iterable<Offset>,
  resolvedBarGeography: ResolvedBarGeography,
  eventAddress: EventAddress
): Area {
  return placeHolderSegments.fold(area) { a, offset ->
    placeholderBarLine(STAVE_HEIGHT)?.let { line ->
      resolvedBarGeography.original.slicePositions[hz(offset)]?.let { pos ->
        val x = pos.xMargin + resolvedBarGeography.barStartGeography.width
        val address = eventAddress.copy(offset = offset, voice = 0)
        val newa = a.addArea(line, Coord(x), address)
        newa.addArea(
          Area(
            resolvedBarGeography.original.slicePositions[hz(offset)]?.width ?: 0,
            STAVE_HEIGHT, tag = "Segment"
          ), Coord(x), address
        )
      }

    } ?: a
  }
}

internal fun DrawableFactory.createMultiBar(
  resolvedBarGeography: ResolvedBarGeography,
  eventAddress: EventAddress
): Area {
  return multiBarArea(
    resolvedBarGeography.segmentWidth,
    resolvedBarGeography.numBars
  )?.let { mba ->
    Area(tag = "Bar").addArea(
      mba.transformEventAddress { _, _ -> eventAddress },
      Coord(resolvedBarGeography.barStartGeography.width + MULTIBAR_OFFSET_X, BLOCK_HEIGHT * 2),
      eventAddress
    )
  } ?: Area()
}

internal fun DrawableFactory.createRepeatBar(
  eventAddress: EventAddress,
  resolvedBarGeography: ResolvedBarGeography,
  repeatBarType: RepeatBarType
): Area {
  val mid = resolvedBarGeography.barStartGeography.width + resolvedBarGeography.segmentWidth / 2
  val repeatBar = repeatBarArea(repeatBarType)
  var area = Area(
    height = STAVE_HEIGHT,
    width = resolvedBarGeography.segmentWidth,
    tag = "Segment",
    addressRequirement = AddressRequirement.SEGMENT,
    event = Event(EventType.BAR)
  )
  return repeatBar?.let {
    val x = if (repeatBarType == RepeatBarType.TWO_START) {
      area.width - repeatBar.width / 2
    } else {
      mid - repeatBar.width / 2
    }

    area = area.addArea(
      repeatBar,
      Coord(x, BLOCK_HEIGHT * 2),
      eventAddress
    )
    Area(tag = "Bar").addArea(
      area,
      Coord(resolvedBarGeography.barStartGeography.width),
      eventAddress = eventAddress.voiceIdless(),
    )
  } ?: area
}
