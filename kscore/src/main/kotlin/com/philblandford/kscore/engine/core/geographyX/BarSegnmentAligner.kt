package com.philblandford.kscore.engine.core.geographyX

import com.philblandford.kscore.engine.core.HorizontalMap
import com.philblandford.kscore.engine.core.SegmentGeography
import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.core.representation.MIN_SEGMENT_CLEARANCE
import com.philblandford.kscore.engine.core.representation.TADPOLE_WIDTH
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.types.SegmentGeogStaveMap
import com.philblandford.kscore.engine.types.hz
import kotlin.math.max


fun alignSegments(
  segmentStaveMap: SegmentGeogStaveMap,
  lyricWidths: Map<Offset, Int> = mapOf(),
  harmonyWidths: Map<Offset, Int> = mapOf(),
  fermataWidths: Map<Offset, Int> = mapOf(),
  segmentExtensionWidths: Map<Offset, Int> = mapOf()
): HorizontalMap {
  val allSegments = segmentStaveMap.flatMap { it.value.toList() }
  val endBar =
    allSegments.maxByOrNull { it.first }?.let { it.first.offset + it.second.duration }
      ?: dZero()
  val allDurations = allSegments.map { it.first.offset }.distinct().sorted().plus(endBar)

  val filtered = segmentStaveMap.map {it.key to it.value.filterNot{it.key.isGrace}}.toMap()
  var basic = getBasicMap(allDurations, filtered, lyricWidths)
  basic = addXMarginExtras(basic, filtered)
  basic = addWidths(basic, harmonyWidths)
  basic = addWidths(basic, fermataWidths)
  basic = addWidthsSimple(basic, segmentExtensionWidths)
  return basic
}


private fun getBasicMap(
  allDurations: Iterable<Offset>,
  segmentStaveMap: SegmentGeogStaveMap,
  lyricWidths: Map<Offset, Int>
): HorizontalMap {

  val widthsByOffset =
    segmentStaveMap.flatMap { it.value.toList() }.groupBy { it.first.offset }.map {
      it.key to it.value.maxByOrNull { it.second.width - it.second.xMargin }?.let { it.second.width - it.second.xMargin }
    }.toMap()

  var total = 0
  return allDurations.windowed(2).map { list ->
    val duration = list[1] - list[0]
    val pos = total
    var width = (widthsByOffset[list[0]] ?: TADPOLE_WIDTH) + getPadding(duration)
    width = max(lyricWidths[list[0]] ?: 0, width)
    total += width
    hz(list[0]) to SlicePosition(pos, pos, width)
  }.toMap().toSortedMap()
}

private fun addXMarginExtras(
  basicMap: HorizontalMap,
  segmentStaveMap: SegmentGeogStaveMap
): HorizontalMap {
  val extraMap = getMarginExtras(basicMap, segmentStaveMap)
  return shiftSlices(basicMap, extraMap)
}

private fun shiftSlices(
  slices: HorizontalMap,
  shiftMap: Map<Offset, Int>
): HorizontalMap {
  var totalShift = 0

  return slices.map { (offset, sp) ->
    val extraX = shiftMap[offset.offset] ?: 0
    val newStart = sp.xMargin + totalShift
    val newXMargin = newStart + (sp.xMargin - sp.start) + extraX
    totalShift += extraX
    offset to SlicePosition(newStart, newXMargin, sp.width + extraX)
  }.toMap()
}

private fun widenSlices(
  slices: HorizontalMap,
  shiftMap: Map<Offset, Int>
): HorizontalMap {
  var totalShift = 0

  return slices.map { (offset, sp) ->
    val extraWidth = shiftMap[offset.offset] ?: 0
    val shift = totalShift
    totalShift += extraWidth
    offset to sp.copy(sp.start + shift, sp.xMargin + shift, sp.width + extraWidth)
  }.toMap()
}

private fun getMarginExtras(
  basicMap: HorizontalMap,
  segmentStaveMap: SegmentGeogStaveMap
): Map<Offset, Int> {
  val extraXMap = mutableMapOf<Offset, Int>()

  segmentStaveMap.forEach { (_, lookup) ->
    val withStart =
      lookup.map { it.key.offset to it.value }.plus(Offset(-1) to SegmentGeography(0, 0, dZero()))
        .sortedBy { it.first }
    withStart.windowed(2).forEach { list ->
      val offset = list[1].first
      val available = basicMap[hz(list[1].first)]?.let { sp1 ->
        basicMap[hz(list[0].first)]?.let { sp0 ->
          sp1.xMargin - (sp0.start + TADPOLE_WIDTH + MIN_SEGMENT_CLEARANCE)
        }
      } ?: 0
      var extra = max(list[1].second.xMargin - available, 0)
      extra = max(extraXMap[offset] ?: 0, extra)
      extraXMap.put(offset, extra)
    }
  }
  return extraXMap
}

private fun addWidthsSimple(
  map: HorizontalMap,
  widths: Map<Offset, Int>
): HorizontalMap {
  var totalShift = 0

  return map.map { (offset, sp) ->
    val width = sp.width + (widths[offset.offset] ?: 0)
    val shift = totalShift
    totalShift += width - sp.width
    offset to sp.copy(sp.start + shift, sp.xMargin + shift, width)
  }.toMap()
}

private fun addWidths(
  map: HorizontalMap,
  widths: Map<Offset, Int>
): HorizontalMap {
  val shifts = getHarmonyShifts(map, widths)
  return widenSlices(map, shifts)
}

private fun getHarmonyShifts(
  map: HorizontalMap,
  harmonyWidths: Map<Offset, Int>
): Map<Offset, Int> {

  val endBar =
    map.toList().maxByOrNull { it.first }?.let { it.second + it.second.width }
      ?: SlicePosition(0, 0, 0)
  val terminatedMap = map.plus(hz(dMax()) to endBar)
  val filtered = harmonyWidths.filterNot { it.value == 0 }.toList().plus(dMax() to 0)

  return filtered.toList().windowed(2).map { list ->
    val available = terminatedMap[hz(list[1].first)]?.let { sp1 ->
      map[hz(list[0].first)]?.let { sp0 ->
        sp1.xMargin - sp0.xMargin
      }
    } ?: 0
    val shift = max(list[0].second - available, 0)
    list[0].first to shift
  }.toMap()

}

fun getPadding(duration: Duration): Int {
  return duration.multiply(300).toInt()
}