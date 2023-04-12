package com.philblandford.kscore.engine.core.geographyX

import com.philblandford.kscore.engine.core.BarGeography
import com.philblandford.kscore.engine.core.HorizontalMap
import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.core.SystemXGeography
import com.philblandford.kscore.engine.core.representation.BAR_EMPTY_WIDTH
import com.philblandford.kscore.engine.core.representation.BAR_START_MARGIN
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.option.getOption
import java.util.*
import kotlin.math.max

/* A directory containing the x-dimensions of bars and systems - the y-dimensions will be known
  later */
data class GeographyXDirectory(
  val sxGeographies: List<SystemXGeography>,
  val barGeographies: Map<Int, BarGeography>,
  val rawBarGeographies: Map<Int, BarGeography> /* before being arranged into multi-bars */
) : GeographyXQuery {
  override fun getSystemXGeographies(): List<SystemXGeography> {
    return sxGeographies
  }
}

fun geographyXDirectory(
  areaDirectoryQuery: AreaDirectoryQuery, scoreQuery: ScoreQuery,
  pageWidth: Int
): GeographyXDirectory {

  val directory = getBarGeographies(areaDirectoryQuery, scoreQuery)
  val headerGeographies = areaDirectoryQuery.getAllHeaderGeogs()
  val preHeaderGeographies = areaDirectoryQuery.getAllPreHeaderGeogs()
  val breaks = getBreaks(scoreQuery)
  val systemGeographies =
    spaceSystems(directory.barGeographies.toSortedMap(), preHeaderGeographies, headerGeographies, breaks, pageWidth)

  return GeographyXDirectory(systemGeographies, directory.barGeographies, directory.rawBarGeographies)
}

fun geographyXDirectoryDiff(
  areaDirectoryQuery: AreaDirectoryQuery, scoreQuery: ScoreQuery,
  pageWidth: Int, old: GeographyXDirectory, bars: List<Int>? = null
): GeographyXDirectory {

  val directory = getBarGeographies(areaDirectoryQuery, scoreQuery, bars, old)
  val headerGeographies = areaDirectoryQuery.getAllHeaderGeogs()
  val preHeaderGeographies = areaDirectoryQuery.getAllPreHeaderGeogs()
  val breaks = getBreaks(scoreQuery)
  val systemGeographies =
    spaceSystems(directory.barGeographies.toSortedMap(), preHeaderGeographies, headerGeographies, breaks, pageWidth)

  return GeographyXDirectory(systemGeographies, directory.barGeographies, directory.rawBarGeographies)
}

/* Get any line/page breaks specified by the user that will override our default bar spacing */
private fun getBreaks(scoreQuery: ScoreQuery): EventHash {
  /* Fixed number of bars per line - add a break event at the specified positions */
  val barsPerLine = getOption<Int>(EventParam.OPTION_BARS_PER_LINE, scoreQuery)
  return if (barsPerLine != 0) {
    createBreaksPerLine(barsPerLine, scoreQuery)
  } else {
    scoreQuery.getEvents(EventType.BREAK) ?: eventHashOf()
  }
}

private fun createBreaksPerLine(barsPerLine: Int, scoreQuery: ScoreQuery): EventHash {
  /* Start counting from first non-upbeat bar */
  val start =
    if (scoreQuery.getTimeSignature(ez(1))?.hidden == true) barsPerLine + 1 else barsPerLine
  return (start..scoreQuery.numBars step barsPerLine).fold(eventHashOf()) { eh, num ->
    eh.plus(
      EMK(EventType.BREAK, ez(num)) to Event(
        EventType.BREAK,
        paramMapOf(EventParam.TYPE to BreakType.SYSTEM)
      )
    )
  }
}

/* Create raw bar geographies - before being stretched to fit on the page */
private fun getBarGeographies(
  areaDirectoryQuery: AreaDirectoryQuery,
  scoreQuery: ScoreQuery,
  bars: List<Int>? = null,
  old: GeographyXDirectory? = null
):  GeographyXDirectory {
  val parts = scoreQuery.allParts(true).toSet()
  val isMultiBar = getOption<Boolean>(EventParam.OPTION_SHOW_MULTI_BARS, scoreQuery)

  val segmentGeographies =
    areaDirectoryQuery.getAllSegmentGeogsByBar()
      .filter { bars == null || bars.contains(it.key) }
      .map {
        it.key to it.value.filter { parts.contains(it.key.main) }
      }

  val lyricWidthsGrouped =
    areaDirectoryQuery.getLyricWidths().toList().groupBy { it.first.barNum }.map {
      it.key to it.value.map { it.first.offset to it.second }.toMap()
    }.toMap()

  val harmonyWidthsGrouped =
    areaDirectoryQuery.getHarmonyWidths().toList().groupBy { it.first.barNum }.map {
      it.key to it.value.map { it.first.offset to it.second }.toMap()
    }.toMap()

  val fermataWidthsGrouped =
    areaDirectoryQuery.getFermataWidths().toList().groupBy { it.first.barNum }.map {
      it.key to it.value.map { it.first.offset to it.second }.toMap()
    }.toMap()

  val segmentExtensionsGrouped =
    areaDirectoryQuery.getSegmentExtensions().toList().groupBy { it.first.barNum }.map {
      it.key to it.value.map { it.first.offset to it.second }.toMap()
    }.toMap()

  val barGeographies = segmentGeographies.map { (bar, map) ->
    val canBeLast = canBeLast(bar, scoreQuery)
    bar to createBarGeography(
      map, lyricWidthsGrouped[bar] ?: mapOf(),
      harmonyWidthsGrouped[bar] ?: mapOf(),
      fermataWidthsGrouped[bar] ?: mapOf(),
      segmentExtensionsGrouped[bar] ?: mapOf(), canBeLast
    )!!
  }.toMap().toSortedMap()

  val rawBarGeographies = ((old?.rawBarGeographies ?: mapOf()) + barGeographies).toSortedMap()

  var allGeographies = rawBarGeographies

  if (isMultiBar) {
    allGeographies = createMultiBars(allGeographies, getMultiBarBreakers(scoreQuery))
  }

  allGeographies = allGeographies.mapNotNull { (k, v) ->
    areaDirectoryQuery.getAllBarStartGeogs()[ez(k)]?.let { startGeogPair ->
      areaDirectoryQuery.getAllBarEndGeogs()[ez(k + v.numBars - 1)]?.let { endGeogPair ->
        k to v.copy(barStartGeographyPair = startGeogPair, barEndGeographyPair = endGeogPair)
      }
    }
  }.toMap().toSortedMap()

  return GeographyXDirectory(listOf(), allGeographies, rawBarGeographies)
}

/* Whether a bar is allowed to be the last in a system - only applies to the first of a 2-bar repeat */
private fun canBeLast(bar: Int, scoreQuery: ScoreQuery): Boolean {
  return !scoreQuery.getAllStaves(true).any { stave ->
    scoreQuery.getParam<Int>(
      EventType.REPEAT_BAR,
      EventParam.NUMBER,
      eas(bar, stave.main, stave.sub)
    ) == 2
  }
}

/* Get the bar number of any event that will cause a multibar to break */
private fun getMultiBarBreakers(scoreQuery: ScoreQuery): Set<Int> {
  /* Any system event (key signature etc) should break a multibar - barline events are
    treated as belonging to the following bar for this purpose
   */
  var events =
    scoreQuery.getSystemEvents().filterNot { it.value.eventType == EventType.BREAK }.map {
      val barNum = it.key.eventAddress.barNum
      when (it.key.eventType) {
        EventType.REPEAT_END, EventType.BARLINE -> barNum + 1
        EventType.NAVIGATION -> {
          when (it.value.subType) {
            NavigationType.DAL_SEGNO, NavigationType.DA_CAPO, NavigationType.FINE -> barNum + 1
            NavigationType.CODA -> {
              if (it.value.isTrue(EventParam.START)) barNum else barNum + 1
            }
            else -> barNum
          }
        }
        else -> it.key.eventAddress.barNum
      }
    }.toSet()
  events = events.plus(scoreQuery.getEvents(EventType.REPEAT_BAR)?.flatMap { (emk, ev) ->
    if (ev.getInt(EventParam.NUMBER) == 1)
      listOf(emk.eventAddress.barNum, emk.eventAddress.barNum + 1)
    else
      listOf(emk.eventAddress.barNum, emk.eventAddress.barNum + 1, emk.eventAddress.barNum + 2)
  } ?: setOf())
  /* Breaks can exist on a per-part level, so aren't always system events */
  return scoreQuery.getEvents(EventType.BREAK)?.map { it.key.eventAddress.barNum + 1 }
    ?.let { barList ->
      events.plus(barList)
    } ?: events
}

/* From all the segments in each bar in a column, create a bar geography that aligns them all */
private fun createBarGeography(
  segmentGeogStaveMap: SegmentGeogStaveMap,
  lyricWidths: Map<Offset, Int>,
  harmonyWidths: Map<Offset, Int>,
  fermataWidths: Map<Offset, Int>,
  segmentExtensionWidths: Map<Offset, Int>,
  canBeLast: Boolean
): BarGeography? {

  var slicePositions =
    alignSegments(
      segmentGeogStaveMap,
      lyricWidths,
      harmonyWidths,
      fermataWidths,
      segmentExtensionWidths
    )
  slicePositions = slicePositions?.let { adjustEmpty(it, lyricWidths, harmonyWidths) }
  slicePositions = slicePositions?.map {
    it.key to SlicePosition(
      it.value.start + BAR_START_MARGIN,
      it.value.xMargin + BAR_START_MARGIN, it.value.width
    )
  }?.toMap()
  return slicePositions?.let {
    BarGeography(
      slicePositions = it,
      canBeLast = canBeLast,
      isEmpty = segmentGeogStaveMap.isEmpty()
    )
  }
}

private fun adjustEmpty(
  slicePositions: HorizontalMap,
  lyricWidths: Map<Offset, Int>, harmonyWidths: Map<Offset, Int>
): HorizontalMap {
  return if (slicePositions.isEmpty()) {
    val extraLyric = lyricWidths.maxByOrNull { it.value }?.value ?: 0
    val extraHarmony = harmonyWidths.maxByOrNull { it.value }?.value
      ?: 0
    val extra = max(max(extraHarmony, extraLyric), BAR_EMPTY_WIDTH)
    if (extra > 1) {
      mapOf(hZero() to SlicePosition(0, 0, extra))
    } else mapOf()
  } else {
    slicePositions
  }
}
