package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.AreaMapKey
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.types.*

data class LineDescriptor(
  val event: Event,
  val eventAddress: EventAddress,
  val start: EventAddress?,
  val end: EventAddress?
)

interface LineDecorator : UpDownDecorator {

  override fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area, drawableFactory: DrawableFactory
  ): Area {
    val filtered = filterEvents(eventHash, stavePositionFinder).toList()
    var areas = filtered.mapNotNull { lineDescriptor ->
      drawableFactory.getArea(lineDescriptor, stavePositionFinder, staveArea)
    }
    areas = adjustAreas(areas)
    return areas.fold(staveArea) { main, (k, a) ->
      main.addEventArea(a.event, a, k.eventAddress, k.coord)
    }
  }

  private fun adjustAreas(areas: List<Pair<AreaMapKey, Area>>): List<Pair<AreaMapKey, Area>> {
    val grouped = areas.groupBy { it.second.event?.isTrue(EventParam.IS_UP) == true }

    return grouped.flatMap { (up, group) ->
      align(up)?.let { align ->
        val yPos = if (align) group.minByOrNull { it.first.coord.y }?.first?.coord?.y else
          areas.maxByOrNull { it.first.coord.y }?.first?.coord?.y
        yPos?.let {
          group.map { it.first.copy(coord = it.first.coord.copy(y = yPos)) to it.second }
        } ?: group

      } ?: areas
    }
  }


  private fun DrawableFactory.getArea(
    lineDescriptor: LineDescriptor,
    stavePositionFinder: StavePositionFinder,
    mainArea: Area
  ): Pair<AreaMapKey, Area>? {
    val startSlice = lineDescriptor.start?.let {
      stavePositionFinder.getSlicePosition(it)
    } ?: stavePositionFinder.getSlicePosition(ez(stavePositionFinder.getStartBar()))

    val endSlice = lineDescriptor.end?.let {
      stavePositionFinder.getSlicePosition(it)
    } ?: SlicePosition(stavePositionFinder.getEndBars(), stavePositionFinder.getEndBars(), 0)

    return startSlice?.let {
      val width = endSlice.xMargin + endSlice.width - startSlice.xMargin - BLOCK_HEIGHT
      getArea(lineDescriptor.event, width, lineDescriptor)?.let { area ->
        val up = isUp(lineDescriptor.eventAddress, lineDescriptor.event)
        val yPos = getYPos(mainArea, startSlice, area, up)
        Pair(AreaMapKey(Coord(startSlice.xMargin, yPos), lineDescriptor.eventAddress), area)
      }
    }
  }

  fun filterEvents(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder
  ): Iterable<LineDescriptor> {
    return eventHash.mapNotNull { (key, event) ->
      event.getParam<Duration>(EventParam.DURATION)?.let { duration ->
        if (event.isTrue(EventParam.END)) {
          stavePositionFinder.getOffsetLookup().subtractDuration(key.eventAddress, duration)
            ?.let { start ->
              evaluateEnd(event, key.eventAddress, start, stavePositionFinder)
            }
        } else {
          evaluateStart(event, key.eventAddress, duration, stavePositionFinder)
        }
      }
    }
  }

  private fun evaluateStart(
    event: Event, eventAddress: EventAddress, duration: Duration,
    stavePositionFinder: StavePositionFinder
  ): LineDescriptor? {
    val startBar = eventAddress.barNum
    var endAddr =
      (stavePositionFinder.getOffsetLookup().addDuration(eventAddress, duration) ?: eventAddress).graceless()
    event.getParam<Offset>(EventParam.GRACE_OFFSET_END)?.let {
      endAddr = endAddr.copy(graceOffset = it)
    }
    return if (startBar >= stavePositionFinder.getStartBar() && startBar <= stavePositionFinder.getEndBar()) {
      val end = if (endAddr.barNum <= stavePositionFinder.getEndBar()) endAddr else null
      LineDescriptor(event, eventAddress, eventAddress, end)
    } else if (endAddr.barNum < stavePositionFinder.getStartBar()) {
      null
    } else if (startBar < stavePositionFinder.getStartBar() && endAddr.barNum > stavePositionFinder.getEndBar()) {
      LineDescriptor(event, eventAddress, null, null)
    } else {
      null
    }
  }

  private fun evaluateEnd(
    event: Event,
    eventAddress: EventAddress, start: EventAddress, stavePositionFinder: StavePositionFinder
  ): LineDescriptor? {
    return if (start.barNum >= stavePositionFinder.getStartBar() && event.duration() != dZero()) {
      null
    } else if (eventAddress.barNum < stavePositionFinder.getStartBar()) {
      null
    } else {
      val end = if (event.duration() == dZero() && !eventAddress.isGrace) {
        stavePositionFinder.getScoreQuery().getNextStaveSegment(eventAddress)?.let { nss ->
          stavePositionFinder.getPreviousSlicePosition(nss)?.first
        }
      } else {
        eventAddress
      }
      if (start.barNum >= stavePositionFinder.getStartBar()) {
        LineDescriptor(event, eventAddress, start, end)
      } else {
        LineDescriptor(event, eventAddress, null, end)
      }
    }
  }

  fun DrawableFactory.getArea(event: Event, width: Int, lineDescriptor: LineDescriptor): Area? = null

  override fun DrawableFactory.getArea(event: Event): Area? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun isUp(eventAddress: EventAddress, event: Event): Boolean {
    return eventAddress.id == 0
  }
}
