package com.philblandford.kscore.engine.core.stave

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.defaultInstrument
import com.philblandford.kscore.api.instrument
import com.philblandford.kscore.engine.core.*
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarEndAreaPair
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarStartAreaPair
import com.philblandford.kscore.engine.core.areadirectory.header.HeaderArea
import com.philblandford.kscore.engine.core.areadirectory.segment.SegmentArea
import com.philblandford.kscore.engine.core.areadirectory.segment.replaceGraceArea
import com.philblandford.kscore.engine.core.areadirectory.segment.replaceStem
import com.philblandford.kscore.engine.core.areadirectory.segment.replaceVoiceArea
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.STAVE_HEIGHT
import com.philblandford.kscore.engine.core.representation.STAVE_MARGIN
import com.philblandford.kscore.engine.core.stave.bar.addBars
import com.philblandford.kscore.engine.core.stave.decoration.collectEventsForDecoration
import com.philblandford.kscore.engine.core.stave.decoration.decorateStave
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.EventType.INSTRUMENT
import com.philblandford.kscore.log.ksLog
import com.philblandford.kscore.log.ksLoge
import java.util.*

data class StaveArea(
  val base: Area, val barPositions: SortedMap<Int, BarPosition>,
  val geog: StaveGeography
) {
}

/* Create a stave area given all the various areas to go on it */
fun DrawableFactory.createStave(
  segmentsForStave: SegmentLookup, headerLookup: Lookup<HeaderArea>,
  barStartLookup: Lookup<BarStartAreaPair>, barEndLookup: Lookup<BarEndAreaPair>,
  systemXGeography: SystemXGeography,
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress,
  isTopPart: Boolean
): StaveArea {

  /* A stave position finder provides a convenient interface to get stave measurements */
  var spf: StavePositionFinder = StavePositionFinderImpl(
    segmentsForStave, systemXGeography, scoreQuery, scoreQuery.singlePartMode(),
    eventAddress.staveId
  )

  val repeatBars = scoreQuery.getRepeatBars()
  val instrument = scoreQuery.getEventAt(INSTRUMENT, eventAddress)?.let { instrument(it.second) }
    ?: defaultInstrument()

  val emptyVoiceMaps =
    scoreQuery.getEmptyVoiceMaps(eventAddress, eventAddress.copy(barNum = systemXGeography.endBar))

  var area = Area(
    tag = "Stave", height = STAVE_HEIGHT + STAVE_MARGIN * 2, yMargin = STAVE_MARGIN,
    numBars = systemXGeography.numBars
  )
  area = addHeader(area, eventAddress, systemXGeography, headerLookup)
  val pair = addBeams(area, scoreQuery, spf)
  area = pair.first
  val segments = pair.second
  spf = spf.replaceSegments(segments)

  area = addBars(
    area,
    segments,
    barStartLookup,
    barEndLookup,
    systemXGeography,
    repeatBars,
    eventAddress,
    emptyVoiceMaps
  )

  area = addStaveLineArea(area, systemXGeography, eventAddress, instrument) ?: area

  val events = collectEventsForDecoration(
    scoreQuery, eventAddress.copy(graceOffset = dZero()),
    systemXGeography.endBar, isTopPart
  )
  area = decorateStave(events, spf, area)
  val geography = createStaveGeography(
    area, segmentsForStave, eventAddress.staveId,
    systemXGeography
  )
  return StaveArea(area, systemXGeography.barPositions, geography)
}

private fun createStaveGeography(
  staveAreaBase: Area,
  segmentsForStave: SegmentLookup,
  staveId: StaveId,
  systemXGeography: SystemXGeography
): StaveGeography {
  val positions = systemXGeography.barPositions.map { (barNum, barPosition) ->
    val segmentPositions =
      barPosition.geog.original.slicePositions.flatMap { (hz, slicePosition) ->
        segmentsForStave[EventAddress(barNum, hz.offset, null, staveId)]?.let { sa ->
          val grace = sa.graceSlicePositions.map {
            Horizontal(0, hz.offset, it.key) to SlicePosition(
              slicePosition.xMargin - it.value.start,
              slicePosition.xMargin - it.value.xMargin, it.value.width
            )
          }
          val adjustedForGrace = SlicePosition(
            slicePosition.start + sa.geography.graceWidth,
            slicePosition.xMargin, slicePosition.width - sa.geography.graceWidth
          )
          grace.plus(Horizontal(0, hz.offset) to adjustedForGrace)
        } ?: listOf()
      }
    barNum to barPosition.copy(
      geog = barPosition.geog.copy(
        original = barPosition.geog.original.copy(
          slicePositions = segmentPositions.toMap()
        )
      )
    )
  }
  return StaveGeography(
    staveAreaBase.height,
    staveAreaBase.yMargin,
    systemXGeography.headerLen,
    positions.toMap().toSortedMap()
  )
}

private fun addHeader(
  area: Area, eventAddress: EventAddress, systemXGeography: SystemXGeography,
  headerLookup: Lookup<HeaderArea>
): Area {
  return headerLookup[eventAddress]?.let {
    area.addArea(
      it.base.transformEventAddress { _, _ -> eventAddress },
      Coord(systemXGeography.preHeaderLen, 0),
      eventAddress
    )
  } ?: area
}

private fun DrawableFactory.lineArea(width: Int): Area? {
  return getDrawable(LineArgs(width, true))?.let {
    Area(it.width, it.height, drawable = it, tag = "StaveLine")
  }
}

private fun DrawableFactory.addStaveLineArea(
  area: Area,
  systemXGeography: SystemXGeography,
  eventAddress: EventAddress,
  instrument: Instrument
): Area? {
  val staveLines =
    staveLinesArea(systemXGeography.width - systemXGeography.preHeaderLen, instrument.staveLines)
  return staveLines?.let { area.addArea(it, Coord(systemXGeography.preHeaderLen, 0), eventAddress) }
}

fun DrawableFactory.staveLinesArea(width: Int, numLines: Int = 5): Area? {
  return lineArea(width)?.let {
    (getStaveLines(numLines)).fold(Area(tag = "StaveLines")) { area, num ->
      area.addArea(it, Coord(0, num * BLOCK_HEIGHT))
    }
  }
}

private fun getStaveLines(numLines: Int): Iterable<Int> {
  return when (numLines) {
    1 -> listOf(4)
    2 -> listOf(2, 6)
    else -> listOf(0, 2, 4, 6, 8)
  }
}

private fun DrawableFactory.addBeams(
  area: Area,
  beamQuery: BeamQuery,
  stavePositionFinder: StavePositionFinder
): Pair<Area, SegmentLookup> {
  val filtered = beamQuery.getBeamsForStave(
    stavePositionFinder.getStartBar(), stavePositionFinder.getEndBar(), stavePositionFinder.staveId
  )
  val (newArea, stemLookup) = drawBeams(filtered, stavePositionFinder, area)

  val newSegments = getNewSegments(
    stavePositionFinder.getSegmentLookup(), stemLookup,
    stavePositionFinder.getScoreQuery()
  )
  return Pair(newArea, newSegments)
}

private const val replaceStemId = "REPLACE_STEM"

/* Replace the stems of beamed chords, whose length may have changed */
private fun DrawableFactory.getNewSegments(
  segmentLookup: SegmentLookup,
  stemLookup: Lookup<StemGeography>,
  scoreQuery: ScoreQuery
): SegmentLookup {
  val mutable = segmentLookup.toMutableMap()
  val replacedGrace = mutableMapOf<EventAddress, SegmentArea>()

  stemLookup.forEach { (eventAddress, stemGeography) ->
    mutable[eventAddress.voiceIdless()]?.let { segmentArea ->

      val newArea =
        if (segmentArea.voiceGeographies[eventAddress.voice]?.stemGeography == stemGeography) segmentArea
        else segmentArea.replaceVoiceArea(
          eventAddress.voice
        ) { k, v ->
          v.replaceStem(
            stemGeography.copy(xPos = stemGeography.xPos - k.coord.x),
            scoreQuery.numVoicesAt(eventAddress), this
          )
        }
      mutable[eventAddress.voiceIdless()] = newArea
      if (eventAddress.isGrace) {
        replacedGrace[eventAddress.voiceIdless()] = newArea
      }
    }
  }

  replacedGrace.toList().groupBy { it.first.graceless() }.forEach { (ea, lookup) ->
    mutable[ea]?.let { sa ->
      val newSa = sa.replaceGraceArea(ea, lookup.toMap())
      mutable[ea] = newSa
    }
  }

  return mutable.toMap()
}