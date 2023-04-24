package com.philblandford.kscore.engine.core.stave.bar

import com.philblandford.kscore.engine.core.BarPosition
import com.philblandford.kscore.engine.core.SystemXGeography
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarEndAreaPair
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarStartAreaPair
import com.philblandford.kscore.engine.core.areadirectory.segment.SegmentArea
import com.philblandford.kscore.engine.types.*

internal fun DrawableFactory.addBars(
  area: Area, segmentLookup: SegmentLookup,
  barStartLookup: Lookup<BarStartAreaPair>,
  barEndLookup: Lookup<BarEndAreaPair>,
  systemXGeography: SystemXGeography,
  repeatBars: Lookup<RepeatBarType>,
  eventAddress: EventAddress,
  emptyVoiceMaps: Iterable<EventAddress>
): Area {
  var areaCopy = area
  val grouped = segmentLookup.toList().groupBy { it.first.barNum }.toSortedMap()
  val emptyMapsGrouped = emptyVoiceMaps.groupBy { it.barNum }

  val bars = (systemXGeography.barPositions).map { (bar, barPosition) ->
    bar to getBar(
      eventAddress, bar, repeatBars, barStartLookup, barEndLookup,
      barPosition, grouped[bar]?.toMap() ?: mapOf(), emptyMapsGrouped[bar] ?: listOf()
    )
  }
  bars.forEach {
    val xPos = systemXGeography.barPositions[it.first]?.pos ?: 0
    areaCopy = areaCopy.addArea(
      it.second, Coord(xPos + systemXGeography.startMain, 0),
      eventAddress = eventAddress.copy(barNum = it.first)
    )
  }
  return areaCopy
}

private fun DrawableFactory.getBar(
  staveAddress: EventAddress, barNum: BarNum,
  repeatBars: Lookup<RepeatBarType>,
  barStartLookup: Lookup<BarStartAreaPair>,
  barEndLookup: Lookup<BarEndAreaPair>,
  barPosition: BarPosition,
  segments: Lookup<SegmentArea>,
  emptyVoiceMaps: Iterable<EventAddress>
): Area {
  val address = staveAddress.copy(barNum = barNum)
  val repeatBar = repeatBars[address]

  val barStartArea = barStartLookup[address] ?: BarStartAreaPair()
  val barEndArea = barEndLookup[address.inc(barPosition.geog.numBars - 1)] ?: BarEndAreaPair()
  val segmentOffsets = segments.filterNot { it.key.isGrace }.map { it.key.offset to it.value }

  return createBar(
    segmentOffsets,
    barPosition.geog,
    barStartArea,
    barEndArea,
    repeatBar,
    address,
    emptyVoiceMaps
  )
}
