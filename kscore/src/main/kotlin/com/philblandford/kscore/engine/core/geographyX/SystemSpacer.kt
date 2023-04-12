package com.philblandford.kscore.engine.core.geographyX

import com.philblandford.kscore.engine.core.*
import com.philblandford.kscore.engine.core.representation.BAR_EMPTY_WIDTH
import com.philblandford.kscore.engine.core.representation.BAR_START_MARGIN
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.Lookup
import java.util.*

fun spaceSystems(
  barGeographies: SortedMap<Int, BarGeography>,
  preHeaderGeographies: Lookup<PreHeaderGeography>,
  headerGeographies: Lookup<HeaderGeography>,
  breaks: EventHash,
  available: Int
): List<SystemXGeography> {

  val preHeadersGrouped = preHeaderGeographies.toList().groupBy { it.first.barNum }
  val headersGrouped = headerGeographies.toList().groupBy { it.first.barNum }
  val systemGeographies = mutableListOf<SystemXGeography>()
  var remaining = barGeographies.values.toList()
  var startBar = barGeographies.toList().sortedBy { it.first }.first().first
  val breakList = breaks.map { it.key.eventAddress.barNum }.toMutableSet()
  while (true) {
    val preHeaderLen = getPreHeaderLen(startBar, preHeadersGrouped)
    val headerLen = getHeaderLen(startBar, headersGrouped)
    val sgReturn =
      getNextSystemGeography(remaining, available, preHeaderLen, headerLen, startBar, breakList)
    if (sgReturn?.remainingBars?.count() == remaining.count()) {
      throw Exception("Infinite loop!")
    }
    if (sgReturn != null) {
      systemGeographies += sgReturn.systemGeography.copy(headerLen = headerLen)
      remaining = sgReturn.remainingBars.toList()
      startBar = sgReturn.systemGeography.endBar + 1
    } else {
      break
    }
  }
  return systemGeographies
}

private fun getPreHeaderLen(
  bar: Int,
  headers: Map<Int, Iterable<Pair<EventAddress, PreHeaderGeography>>>
): Int {
  return headers[bar]?.let { it.maxByOrNull { it.second.width } }?.second?.width ?: 0
}

private fun getHeaderLen(
  bar: Int,
  headers: Map<Int, Iterable<Pair<EventAddress, HeaderGeography>>>
): Int {
  return headers[bar]?.let { it.maxByOrNull { it.second.width } }?.second?.width ?: 0
}

private data class SGReturn(
  val systemGeography: SystemXGeography,
  val remainingBars: Iterable<BarGeography>
)

private fun getNextSystemGeography(
  barGeographies: Iterable<BarGeography>, totalAvailable: Int,
  preHeaderLen: Int,
  headerLen: Int,
  startBar: Int,
  breaks: MutableSet<Int>
): SGReturn? {
  val available = totalAvailable - headerLen - preHeaderLen
  val geogList = barGeographies.toList()
  var geogIdx = 0
  if (geogList.isEmpty()) {
    return null
  }

  var barNum = startBar
  var position = 0
  var positions = mutableMapOf<Int, BarPosition>()

  while (geogIdx < geogList.size) {
    val geog = geogList[geogIdx]
    val first = barNum == startBar
    val barWidth = geog.width(first, true)
    if ((position + barWidth > available || breaks.contains(barNum - 1)) && barNum != startBar) {
      if (geogIdx > 0 && !geogList[geogIdx - 1].canBeLast) {
        positions.remove(barNum - 1)
        barNum -= 1
      }
      breaks.remove(barNum - 1)
      break
    } else {
      val resolved = resolveBarGeography(geog, first, false)
      positions[barNum] = BarPosition(position, resolved)
      position += geog.width(first, false)
      geogIdx++
    }
    barNum += geog.numBars
  }
  val endBar = barNum - 1

  positions = positions.map {
    if (it.key == endBar) {
      it.key to it.value.copy(
        geog = it.value.geog.copy(
          last = true,
          barEndGeography = it.value.geog.original.barEndGeographyPair.endStave
        )
      )
    } else {
      it.key to it.value
    }
  }.toMap().toMutableMap()

  val unstretchedWidth = positions.toList().lastOrNull()?.let {
    it.second.pos + it.second.geog.width
  } ?: 1
  val stretch = available.toFloat() / unstretchedWidth

  val geog = convertSystemXGeography(startBar, endBar, preHeaderLen, headerLen, positions, stretch)
  return SGReturn(geog, geogList.drop(positions.size))
}

private fun convertSystemXGeography(
  startBar: Int, endBar: Int, preHeaderLen: Int, headerLen: Int, positions: Map<Int, BarPosition>,
  stretch: Float
): SystemXGeography {

  var total = 0
  val stretchedPositions = positions.toList().withIndex().map { iv ->
    val posX = iv.value.first
    val barPos = iv.value.second
    val sPositions = barPos.geog.original.slicePositions
    val effectiveStretch = effectiveStretch(
      barPos.geog.original,
      sPositions.toSortedMap().toList().map { it.second },
      stretch,
      iv.index == 0,
      iv.index == positions.size - 1
    )

    val stretchedSlices = sPositions.map {
      it.key to getNewSlicePos(it.value, effectiveStretch)
    }.toMap()
    val offset = total
    val rbg = ResolvedBarGeography(
      barPos.geog.barStartGeography,
      barPos.geog.barEndGeography,
      barPos.geog.first,
      barPos.geog.last,
      barPos.geog.original.copy(slicePositions = stretchedSlices),
      effectiveStretch
    )
    total += rbg.width
    posX to BarPosition(offset, rbg)
  }.toMap().toSortedMap()
  return SystemXGeography(startBar, endBar, preHeaderLen, headerLen, stretchedPositions, stretch)
}

private fun effectiveStretch(
  barGeography: BarGeography,
  slicePositions: Iterable<SlicePosition>,
  stretch: Float,
  first: Boolean,
  last: Boolean
): Float {
  val margin = if (slicePositions.count() > 0) BAR_START_MARGIN else 0
  val fixed = barGeography.start(first) + barGeography.end(last) + margin
  val posWidth =
    slicePositions.lastOrNull()?.let { it.start + it.width - BAR_START_MARGIN } ?: BAR_EMPTY_WIDTH
  return (stretch * (fixed + posWidth) - fixed) / posWidth
}

private fun getNewSlicePos(slicePosition: SlicePosition, stretch: Float): SlicePosition {

  val real = SlicePosition(
    slicePosition.start - BAR_START_MARGIN, slicePosition.xMargin - BAR_START_MARGIN,
    slicePosition.width
  )
  val start = (real.start * stretch).toInt() + BAR_START_MARGIN
  return SlicePosition(
    start,
    (start + (real.xMargin - real.start)).toInt(),// - real.start + BAR_START_MARGIN).toInt(),
    (real.width * stretch).toInt()
  )
}

private fun resolveBarGeography(
  barGeography: BarGeography,
  first: Boolean,
  last: Boolean
): ResolvedBarGeography {
  val barStart =
    if (first) barGeography.barStartGeographyPair.startStave else barGeography.barStartGeographyPair.notStartStave
  val barEnd =
    if (last) barGeography.barEndGeographyPair.endStave else barGeography.barEndGeographyPair.notEndStave
  return ResolvedBarGeography(barStart, barEnd, first, last, barGeography)
}
