package com.philblandford.kscore.engine.core.stave

import com.philblandford.kscore.engine.core.*
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.types.*


internal class StavePositionFinderImpl(
  private val segmentAreaLookup: SegmentLookup,
  private val systemXGeography: SystemXGeography,
  private val query: ScoreQuery,
  override val singlePartMode: Boolean,
  override val staveId: StaveId,
) : StavePositionFinder {
  private val startBars = systemXGeography.startMain

  override fun getSlicePosition(eventAddress: EventAddress): SlicePosition? {
    return systemXGeography.barPositions[eventAddress.barNum]?.let { barPos ->
      val start = barPos.pos + barPos.geog.barStartGeography.width + startBars

      barPos.geog.original.slicePositions[hz(eventAddress.offset)]?.let { slicePos ->
        val sp = SlicePosition(
          barPos.pos + slicePos.start + startBars + barPos.geog.barStartGeography.width,
          barPos.pos + slicePos.xMargin + startBars + barPos.geog.barStartGeography.width,
          slicePos.width
        )
        if (eventAddress.isGrace) {
          getSlicePositionGrace(eventAddress, sp)
        } else {
          sp
        }
      } ?: SlicePosition(start, start, barPos.geog.segmentWidth)
    }
  }

  private fun getSlicePositionGrace(
    eventAddress: EventAddress,
    slicePosition: SlicePosition
  ): SlicePosition? {
    return eventAddress.graceOffset?.let { go ->
      getSegmentGeography(eventAddress.graceless())?.let { geog ->
        geog.graceSlicePositions[go]?.let { graceSlice ->
          SlicePosition(
            slicePosition.xMargin - graceSlice.start,
            slicePosition.xMargin - graceSlice.xMargin,
            graceSlice.width
          )
        }
      }
    }
  }

  override fun getLastSlicePosition(): SlicePosition? {
    return systemXGeography.barPositions[getEndBar()]?.let { barPos ->
      barPos.geog.original.slicePositions.toList().maxByOrNull { it.first }?.second?.let { slice ->
        slice.copy(
          slice.start + barPos.pos + getStartBars(),
          slice.xMargin + barPos.pos + getStartBars()
        )
      }
        ?: SlicePosition(
          barPos.pos,
          barPos.pos,
          barPos.geog.width
        )
    }
  }

  override fun getPreviousSlicePosition(eventAddress: EventAddress): Pair<EventAddress, SlicePosition>? {
    return systemXGeography.slicePositions.takeWhile { it.first < eventAddress.staveless() }
      .lastOrNull()
  }

  override fun getFirstSegmentGeography(): SegmentGeography? {
    val start = eas(systemXGeography.startBar, dZero(), staveId)
    return getSegmentGeography(start)
  }

  override fun getLastSegmentGeography(): SegmentGeography? {
    val end = getLastSegment()
    return getSegmentGeography(end)
  }

  override fun getFirstSegment(): EventAddress {
    return eas(systemXGeography.startBar, staveId = staveId)
  }

  override fun getLastSegment(): EventAddress {
    return eas(systemXGeography.endBar, systemXGeography.lastSlice.offset, staveId)
  }

  override fun getStemGeography(eventAddress: EventAddress): StemGeography? {
    return segmentAreaLookup[eventAddress.voiceless()]?.let {
      val pos = it.geography.voicePositions[eventAddress.voice] ?: 0
      it.voiceGeographies[eventAddress.voice]?.stemGeography?.let { sg ->
        sg.copy(xPos = sg.xPos + pos)
      }
    }
  }

  override fun getSegmentGeography(eventAddress: EventAddress): SegmentGeography? {
    return segmentAreaLookup[eventAddress.voiceIdless()]?.geography
  }

  override fun getVoiceGeography(eventAddress: EventAddress): VoiceGeography? {
    return getSegmentGeography(eventAddress)?.let { it.voiceGeographies[eventAddress.voice] }
  }

  override fun getBarPosition(barNum: Int, end:Boolean): BarPosition? {
    return systemXGeography.barPositions.toList().find {
      if (end) {
        it.first + it.second.geog.numBars - 1 == barNum
      } else {
        it.first == barNum
      }
    }
      ?.second?.let {
      it.copy(pos = it.pos + startBars)
    }
  }

  override fun getStartBars(): Int {
    return startBars
  }

  override fun getEndBars(): Int {
    return systemXGeography.width
  }

  override fun getStartBar(): Int {
    return systemXGeography.startBar
  }

  override fun getEndBar(): Int {
    return systemXGeography.endBar
  }

  override fun getOffsetLookup(): OffsetLookup {
    return query
  }

  override fun getSegmentLookup(): SegmentLookup {
    return segmentAreaLookup
  }

  override fun getScoreQuery(): ScoreQuery {
    return query
  }

  override fun replaceSegments(newSegments: SegmentLookup): StavePositionFinder {
    return StavePositionFinderImpl(newSegments, systemXGeography, query, singlePartMode, staveId)
  }
}
