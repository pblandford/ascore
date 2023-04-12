package com.philblandford.kscore.engine.core

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.semibreve
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.EventParam.*
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

typealias OffsetMap = Map<Offset, SlicePosition>
typealias HorizontalMap = Map<Horizontal, SlicePosition>

/* A Geography data class allows us to represention the numerical properties of an area,
  without having to pass around a bulky area object with all its children etc
 */

fun OffsetMap.width(): Int {
  return toList().lastOrNull()?.let {
    it.second.start + it.second.width
  } ?: 0
}

sealed class Geography

data class PreHeaderGeography(val textWidth: Int, val joinWidth: Int) : Geography() {
  val width = textWidth + joinWidth
  val joinStart = width - joinWidth
}

data class HeaderGeography(val keyWidth: Int, val timeWidth: Int, val clefWidth: Int) :
  Geography() {
  val width = keyWidth + timeWidth + clefWidth + STAVE_HEADER_GAP * 4//FIXME
  val clefStart = 0
  val keyStart = clefStart + clefWidth
  val timeStart = keyStart + keyWidth
}

data class StemGeography(
  val tip: Int,
  val base: Int,
  val noteSpan: Int,
  val xPos: Int,
  val up: Boolean,
  val beamHeight: Int = 0
) : Geography() {
  val height = abs(base - tip)
  val exposed = height - noteSpan
  val noteArea = height - exposed
  val yPos = if (up) tip else base
}

data class VoiceGeography(
  val width: Int, val xMargin: Int, val duration: Duration, val stemGeography: StemGeography?,
  val notePositions: Iterable<Coord>, val restPos: Int? = null, val restHeight: Int? = null,
  val articulationHeight: Int = 0, val articulationAbove: Boolean? = null,
  val beamed: Boolean = false
) : Geography() {
  val topNote = notePositions.minByOrNull { it.y }?.y ?: 0
  val topStem = stemGeography?.yPos ?: 0
  val top = restPos ?: min(topNote, topStem)
  val bottomNote = notePositions.maxByOrNull { it.y }?.y ?: 0
  private val bottomStem = stemGeography?.let { it.yPos + it.height } ?: 0
  val bottom = restPos?.let { it + (restHeight ?: 0) } ?: max(bottomNote, bottomStem)
  val isRest = notePositions.count() == 0
}

data class SegmentGeography(
  val width: Int,
  val xMargin: Int,
  val duration: Duration,
  val voiceGeographies: Map<Int, VoiceGeography> = mapOf(),
  val voicePositions: Map<Int, Int> = mapOf(),
  val graceSlicePositions: Map<Offset, SlicePosition> = mapOf(),
  val placeHolder: Boolean = false
) : Geography() {
  val topGeog = voiceGeographies.minByOrNull { it.value.topNote }
  val topNote = topGeog?.value?.topNote ?: 0
  val bottomGeog = voiceGeographies.maxByOrNull { it.value.bottomNote }
  val bottomNote = bottomGeog?.value?.bottomNote ?: 0
  val graceWidth = graceSlicePositions.toSortedMap().toList().lastOrNull()
    ?.let { it.second.start + it.second.width } ?: 0
  val hasUpStem =
    voiceGeographies.count { it.value.duration < semibreve() && it.value.stemGeography?.up == true } > 0
  val hasDownStem =
    voiceGeographies.count { it.value.duration < semibreve() && it.value.stemGeography?.up == false } > 0
}

data class SlicePosition(val start: Int, val xMargin: Int, val width: Int) {
  operator fun plus(num: Int): SlicePosition {
    return SlicePosition(start + num, xMargin + num, width)
  }

  operator fun minus(num:Int):SlicePosition {
    return SlicePosition(start - num, xMargin - num, width)
  }
}

data class BarGeography(
  val barStartGeographyPair: BarStartGeographyPair = BarStartGeographyPair(),
  val barEndGeographyPair: BarEndGeographyPair = BarEndGeographyPair(),
  val slicePositions: HorizontalMap = mapOf(),
  val isEmpty:Boolean = false,
  val canBeLast: Boolean = true,
  val numBars: Int = 1
) : Geography() {
  private val startIfFirst = barStartGeographyPair.startStave.width
  private val startIfNotFirst = barStartGeographyPair.notStartStave.width
  private val endIfLast = barEndGeographyPair.endStave.width
  private val endIfNotLast = barEndGeographyPair.notEndStave.width
  private val last = slicePositions.maxByOrNull { it.key.offset }?.value
  val segmentWidth = last?.let {
    it.start + it.width
  } ?: BAR_EMPTY_WIDTH

  fun width(first: Boolean, last: Boolean): Int {
    return start(first) + end(last) + segmentWidth
  }

  fun start(first: Boolean) = if (first) startIfFirst else startIfNotFirst
  fun end(last: Boolean) = if (last) endIfLast else endIfNotLast
}

data class BarStartGeographyPair(
  val startStave: BarStartGeography = BarStartGeography(),
  val notStartStave: BarStartGeography = BarStartGeography()
)

data class BarStartGeography(
  val barLineWidth: Int = 0, val repeatDotWidth: Int = 0,
  val keyWidth: Int = 0, val timeWidth: Int = 0
) : Geography() {
  val keyStart =
    barLineWidth + repeatDotWidth + if (keyWidth > 0 || timeWidth > 0) STAVE_HEADER_GAP else 0
  val timeStart = keyStart + keyWidth
  val width = timeStart + timeWidth
}


data class BarEndGeographyPair(
  val endStave: BarEndGeography = BarEndGeography(),
  val notEndStave: BarEndGeography = BarEndGeography()
)

data class BarEndGeography(
  val keyWidth: Int = 0, val timeWidth: Int = 0, val clefWidth: Int = 0,
  val repeatDotWidth: Int = 0, val barLineWidth: Int = 0
) : Geography() {
  val width = keyWidth + timeWidth + clefWidth + repeatDotWidth + barLineWidth
  val keyStart = 0
  val timeStart = keyStart + keyWidth
  val clefStart = timeStart + timeWidth
  val repeatDotStart = clefStart + clefWidth
}

data class ResolvedBarGeography(
  val barStartGeography: BarStartGeography = BarStartGeography(),
  val barEndGeography: BarEndGeography = BarEndGeography(),
  val first: Boolean = false, val last: Boolean = false,
  val original: BarGeography = BarGeography(),
  val stretch: Float = 1f,
  val segmentWidth:Int =
    if (original.slicePositions.isEmpty()) (original.segmentWidth * stretch).toInt() else
      original.segmentWidth
) : Geography() {

  val width = barStartGeography.width + barEndGeography.width + segmentWidth
  val numBars = original.numBars
}


data class BarPosition(val pos: Int, val geog: ResolvedBarGeography)

data class SystemXGeography(
  val startBar: Int, val endBar: Int, val preHeaderLen: Int, val headerLen: Int,
  val barPositions: SortedMap<Int, BarPosition>, val stretch: Float
) : Geography() {
  val width = calcWidth()
  val startMain = preHeaderLen + headerLen
  val numBars = endBar - startBar + 1
  val slicePositions = barPositions.flatMap { bp ->
    var sps = bp.value.geog.original.slicePositions
    if (sps.isEmpty()) {
      sps = mapOf(Horizontal(1, dZero()) to SlicePosition(0, 0, 0))
    }
    sps.map { (o, sp) ->
      ez(bp.key, o.offset) to sp
    }
  }
  val lastSlice =
    barPositions.toSortedMap().toList()
      .lastOrNull()?.second?.geog?.original?.slicePositions?.toSortedMap()?.toList()
      ?.lastOrNull()?.first
      ?: hz(dZero())

  private fun calcWidth(): Int {
    val last = barPositions.toList().lastOrNull()?.second
    return (last?.pos ?: 0) + (last?.geog?.width ?: 0) + headerLen + preHeaderLen
  }
}


data class StaveGeography(
  val height: Int, val yMargin: Int,
  val headerLen: Int,
  val barPositions: Map<Int, BarPosition>
) : Geography()

data class StavePosition(val pos: Int, val staveGeography: StaveGeography)

data class PartGeography(
  val height: Int, val yMargin: Int, val stavePositions: SortedMap<Int, StavePosition>,
  val numSegments: Int, val startBar: Int, val endBar: Int
) : Geography() {
  val mainHeight = run {
    val list = stavePositions.toList().sortedBy { it.first }
    val bottom = list.lastOrNull()?.let { it.second.pos + STAVE_HEIGHT } ?: 0
    val top = list.firstOrNull()?.second?.pos ?: 0
    bottom - top
  }
}

data class PartPosition(val pos: Int, val partGeography: PartGeography)

data class SystemYGeography(
  val xGeog: SystemXGeography,
  val partPositions: SortedMap<Int, PartPosition>
) : Geography() {
  private val positionsAsList = partPositions.toList()
  val yMargin = positionsAsList.first().second?.partGeography?.yMargin ?: 0
  val height = positionsAsList.last().let { it.second.pos + it.second.partGeography.height }
}

data class SystemPosition(val pos: Int, val systemYGeography: SystemYGeography)


data class PageGeography(
  val pageNum: Int,
  val layoutDescriptor: LayoutDescriptor,
  val systemPositions: SortedMap<Int, SystemPosition>
) : Geography() {
  private val sorted = systemPositions.toList().sortedBy { it.first }
  val startBar: Int = sorted.firstOrNull()?.first ?: 1
  val endBar: Int = sorted.lastOrNull()?.second?.systemYGeography?.xGeog?.endBar ?: startBar
}

data class ScoreGeography(val pages: Map<Int, PageGeography>)

data class LayoutDescriptor(
  val pageWidth: Int = PAGE_WIDTH,
  val pageHeight: Int = (pageWidth * PAGE_RATIO).toInt(),
  val leftMargin: Int = PAGE_LEFT_MARGIN,
  val rightMargin: Int = PAGE_RIGHT_MARGIN,
  val topMargin: Int = PAGE_TOP_MARGIN,
  val bottomMargin: Int = PAGE_BOTTOM_MARGIN,
  val header: Int = 0, val footer: Int = 0,
  val titleWidth: Int = pageWidth - PAGE_LEFT_MARGIN - PAGE_RIGHT_MARGIN,
  val titleHeight: Int = TITLE_AREA_HEIGHT,
  val staveGap: Int = STAVE_GAP,
  val systemGap: Int = SYSTEM_GAP
) {
  fun toEvent(): Event {
    val params = paramMapOf(
      LAYOUT_PAGE_WIDTH to pageWidth,
      LAYOUT_PAGE_HEIGHT to pageHeight,
      LAYOUT_TOP_MARGIN to topMargin,
      LAYOUT_BOTTOM_MARGIN to bottomMargin,
      LAYOUT_LEFT_MARGIN to leftMargin,
      LAYOUT_RIGHT_MARGIN to rightMargin,
      LAYOUT_STAVE_GAP to staveGap,
      LAYOUT_SYSTEM_GAP to systemGap
    )
    return Event(EventType.LAYOUT, params)
  }
}

fun layoutDescriptor(event: Event): LayoutDescriptor {
  val pageWidth = event.getParam<Int>(LAYOUT_PAGE_WIDTH) ?: PAGE_WIDTH
  val pageHeight = event.getParam<Int>(LAYOUT_PAGE_HEIGHT) ?: (pageWidth * PAGE_RATIO).toInt()
  val topMargin = event.getParam<Int>(LAYOUT_TOP_MARGIN) ?: PAGE_TOP_MARGIN
  val bottomMargin = event.getParam<Int>(LAYOUT_BOTTOM_MARGIN) ?: PAGE_BOTTOM_MARGIN
  val leftMargin = event.getParam<Int>(LAYOUT_LEFT_MARGIN) ?: PAGE_LEFT_MARGIN
  val rightMargin = event.getParam<Int>(LAYOUT_RIGHT_MARGIN) ?: PAGE_RIGHT_MARGIN
  val staveGap = event.getParam<Int>(LAYOUT_STAVE_GAP) ?: STAVE_GAP
  val systemGap = event.getParam<Int>(LAYOUT_SYSTEM_GAP) ?: SYSTEM_GAP
  return LayoutDescriptor(
    pageWidth, pageHeight, leftMargin, rightMargin, topMargin, bottomMargin,
    staveGap = staveGap, systemGap = systemGap
  )
}

fun getLayoutDescriptor(scoreQuery: ScoreQuery): LayoutDescriptor {
  return scoreQuery.getEvent(EventType.LAYOUT, eZero())?.let {
    layoutDescriptor(it)
  } ?: LayoutDescriptor()
}