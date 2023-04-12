package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.SegmentGeography
import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.SlurArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.SLUR_OVERHANG
import com.philblandford.kscore.engine.core.representation.SLUR_OVERHANG_VERTICAL
import com.philblandford.kscore.engine.core.representation.STAVE_HEIGHT
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.StavePositionFinder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private data class Adjustment(val start: Coord, val mid: Coord, val end: Coord)

object SlurDecorator : LineDecorator {

  override fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area, drawableFactory: DrawableFactory
  ): Area {
    var copy = staveArea
    val filtered = filterEvents(eventHash, stavePositionFinder)
    filtered.forEach { ld ->
      drawableFactory.getSlur(ld, stavePositionFinder, staveArea)?.let { (coord, area) ->
        copy = copy.addArea(area, coord, eventAddress = ld.eventAddress)
      }
    }
    return copy
  }

  private fun DrawableFactory.getSlur(
    lineDescriptor: LineDescriptor,
    stavePositionFinder: StavePositionFinder,
    mainArea: Area
  ): Pair<Coord, Area>? {
    return getStartPos(lineDescriptor.start, stavePositionFinder)?.let { startPos ->
      getEndPos(lineDescriptor.end, stavePositionFinder)?.let { endPos ->
        slurArea(
          lineDescriptor,
          startPos,
          endPos,
          stavePositionFinder,
          mainArea
        )?.let { (area, top) ->
          Coord(startPos.xMargin + top.x, top.y) to area.copy(
            tag = "Slur",
            addressRequirement = AddressRequirement.EVENT, event = lineDescriptor.event
          )
        }
      }
    }
  }

  private fun getStartPos(
    address: EventAddress?,
    stavePositionFinder: StavePositionFinder
  ): SlicePosition? {
    return address?.let {
      stavePositionFinder.getSlicePosition(it)
    } ?: run {
      val x = stavePositionFinder.getStartBars() - SLUR_OVERHANG
      SlicePosition(x, x, 0)
    }
  }

  private fun getEndPos(
    endAddress: EventAddress?,
    stavePositionFinder: StavePositionFinder
  ): SlicePosition? {
    return endAddress?.let {
      stavePositionFinder.getSlicePosition(it)
    } ?: run {
      val x = stavePositionFinder.getEndBars() + SLUR_OVERHANG
      SlicePosition(x, x, 0)
    }
  }

  private fun DrawableFactory.slurArea(
    lineDescriptor: LineDescriptor,
    startPos: SlicePosition, endPos: SlicePosition, stavePositionFinder: StavePositionFinder,
    mainArea: Area
  ): Pair<Area, Coord>? {
    val adjustment = getAdjustment(lineDescriptor.event)
    val up = lineDescriptor.event.isTrue(EventParam.IS_UP)
    val eventAddress = lineDescriptor.eventAddress
    val endAddress = lineDescriptor.end

    val consecutive = stavePositionFinder.getScoreQuery().getNextStaveSegment(eventAddress) ==
        endAddress
    val width = endPos.xMargin - startPos.xMargin
    val startGeog =
      if (lineDescriptor.start == null) stavePositionFinder.getFirstSegmentGeography() else
        stavePositionFinder.getSegmentGeography(eventAddress)
    val endGeog = if (lineDescriptor.end == null) stavePositionFinder.getLastSegmentGeography() else
      endAddress?.let { stavePositionFinder.getSegmentGeography(it) }
    val startCoord =
      getStartCoord(startGeog, endGeog, lineDescriptor.start == null, consecutive, up)
    val endCoord = getEndCoord(startGeog, endGeog, lineDescriptor.end == null, consecutive, up)
    val midPoint = getMidPointBulge(
      width, startPos.xMargin, endPos.xMargin,
      most(startCoord.y, endCoord.y, up), up, mainArea
    )
    val midY = if (up) min(startCoord.y, endCoord.y) - midPoint
    else max(startCoord.y, endCoord.y) + midPoint
    val top = if (up) midY + adjustment.mid.y else min(startCoord.y, endCoord.y)
    return getDrawableArea(
      SlurArgs(
        Coord(startCoord.x, startCoord.y).plus(adjustment.start),
        Coord(width / 2 + startCoord.x, midY).plus(adjustment.mid),
        Coord(width + endCoord.x, endCoord.y).plus(adjustment.end),
        up
      )
    )?.let { it to Coord(startCoord.x + adjustment.start.x, top) }
  }

  private fun getMidPointBulge(
    width: Int,
    start: Int, end: Int, from: Int, up: Boolean,
    staveArea: Area
  ): Int {
    val forEvent = if (up) {
      staveArea.getTopForRangeNullable(start, end) { it.tag == "StaveLines" }?.let { from - it }
        ?: 0
    } else {
      staveArea.getBottomForRangeNullable(start, end)?.let { it - from } ?: 0
    }
    return max((width.toFloat() / 10).toInt(), abs(forEvent))
  }

  private fun getStartCoord(
    geog: SegmentGeography?,
    endGeog: SegmentGeography?,
    leftOverhang: Boolean,
    consecutive: Boolean,
    up: Boolean
  ): Coord {
    return if (up) getStartCoordUp(geog, endGeog, leftOverhang, consecutive) else
      getStartCoordDown(geog)
  }

  private fun getEndCoord(
    geog: SegmentGeography?,
    endGeog: SegmentGeography?,
    rightOverhang: Boolean,
    consecutive: Boolean,
    up: Boolean
  ): Coord {
    return if (up) getEndCoordUp(geog, endGeog, rightOverhang, consecutive) else
      getEndCoordDown(geog, endGeog, consecutive)
  }

  private fun getStartCoordUp(
    geog: SegmentGeography?,
    endGeog: SegmentGeography?,
    leftOverhang: Boolean,
    consecutive: Boolean
  ): Coord {
    return geog?.let {
      if (geog.hasUpStem && !(consecutive && endGeog?.hasUpStem == false)) {
        aboveStem(geog)
      } else {
        aboveTop(geog, leftOverhang)
      }
    } ?: Coord()
  }

  private fun getStartCoordDown(
    geog: SegmentGeography?
  ): Coord {
    return geog?.let {
      if (geog.hasDownStem) {
        belowStem(geog)
      } else {
        Coord(BLOCK_HEIGHT, belowBottom(geog))
      }
    } ?: Coord(0, STAVE_HEIGHT + BLOCK_HEIGHT)
  }

  private fun getEndCoordUp(
    startGeog: SegmentGeography?,
    geog: SegmentGeography?,
    rightOverhang: Boolean,
    consecutive: Boolean
  ): Coord {
    return geog?.let {
      if (geog.hasUpStem && !(consecutive && startGeog?.hasUpStem == false)) {
        aboveStem(geog)
      } else {
        aboveTop(geog, rightOverhang)
      }
    } ?: Coord(0, 0)
  }

  private fun getEndCoordDown(
    startGeog: SegmentGeography?,
    endGeog: SegmentGeography?,
    consecutive: Boolean
  ): Coord {
    return endGeog?.let { geog ->

      if (geog.hasDownStem && !(consecutive && startGeog?.hasDownStem == false)) {
        belowStem(geog)
      } else {
        val x = if (consecutive) -BLOCK_HEIGHT / 2 else BLOCK_HEIGHT
        Coord(x, belowBottom(geog))
      }

    } ?: Coord(0, STAVE_HEIGHT + BLOCK_HEIGHT)
  }

  private fun aboveStem(geog: SegmentGeography): Coord {
    val stem =
      geog.voiceGeographies.minByOrNull { it.value.stemGeography?.tip ?: 0 }?.value?.stemGeography
    val x = (stem?.xPos ?: 0) - BLOCK_HEIGHT
    var y = (stem?.tip ?: 0) - BLOCK_HEIGHT
    if (geog.topGeog?.value?.articulationAbove == true) {
      y -= (geog.topGeog.value.articulationHeight ?: 0)
    }
    return Coord(x, y)
  }

  private fun belowStem(geog: SegmentGeography): Coord {
    val stem = geog.voiceGeographies.maxByOrNull { it.value.stemGeography?.tip ?: STAVE_HEIGHT }
      ?.value?.stemGeography
    val x = stem?.xPos ?: 0
    val y =
      (stem?.tip ?: STAVE_HEIGHT) + BLOCK_HEIGHT + (geog.bottomGeog?.value?.articulationHeight
        ?: 0)
    return Coord(x, y)
  }

  private fun aboveTop(geog: SegmentGeography, overhang: Boolean): Coord {
    val x = BLOCK_HEIGHT
    var y =
      (geog.topGeog?.let { it.value.topNote - it.value.articulationHeight } ?: 0) - BLOCK_HEIGHT
    if (overhang) {
      y = min(y, -SLUR_OVERHANG_VERTICAL)
    }
    return Coord(x, y)
  }

  private fun belowBottom(geog: SegmentGeography): Int {
    return (geog.bottomGeog?.let { it.value.bottomNote + it.value.articulationHeight }
      ?: 0) + BLOCK_HEIGHT * 3
  }

  private fun getAdjustment(event: Event): Adjustment {
    val start = event.getParam<Coord>(EventParam.HARD_START) ?: Coord()
    val mid = event.getParam<Coord>(EventParam.HARD_MID) ?: Coord()
    val end = event.getParam<Coord>(EventParam.HARD_END) ?: Coord()
    return Adjustment(start, mid, end)
  }

  private fun most(a: Int, b: Int, up: Boolean): Int {
    return if (up) min(a, b) else max(a, b)
  }
}