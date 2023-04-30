package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.SegmentGeography
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.AreaDirectory
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.EventList
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.util.toImmutableMap
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.immutableMapOf
import kotlinx.collections.immutable.persistentMapOf

internal data class SegmentCreatorReturn(
  val segmentLookup: Lookup<SegmentArea>,
  val segmentGeogBarLookup: SegmentGeogBarLookup,
  val segmentStaveLookup: SegmentStaveLookup
)

internal fun DrawableFactory.createSegments(
  scoreQuery: ScoreQuery,
  existing: AreaDirectory? = null,
  changedBars: Iterable<EventAddress>? = null
): SegmentCreatorReturn? {

  val segments = createSegmentAreas(scoreQuery, existing, changedBars)
  val geogs = segments.map { Pair(it.key, it.value.geography) }.toMap()

  val segmentStaveLookup = createStaveLookup(segments)
  val segmentGeogBarLookup = createBarLookup(geogs, scoreQuery.numBars, existing, changedBars?.map { it.barNum })

  return SegmentCreatorReturn(segments, segmentGeogBarLookup, segmentStaveLookup)

}

private fun DrawableFactory.createSegmentAreas(
  scoreQuery: ScoreQuery, existing: AreaDirectory?,
  changedBars: Iterable<EventAddress>?
): Lookup<SegmentArea> {
  val addresses = changedBars ?: scoreQuery.allBarAddresses(existing != null)
  val repeatBars = scoreQuery.getRepeatBars()

  val segments = addresses.flatMap { bar ->
    if (repeatBars[bar] != null) {
      listOf()
    } else {
      createSegmentsForBar(bar, scoreQuery)
    }
  }.toMap()
  val existingSegments = existing?.segmentLookup ?: mapOf()
  val previous = changedBars?.let {
    existingSegments.filterNot { changedBars.contains(it.key.startBar()) }
  } ?: existingSegments
  return previous.plus(segments)
}

fun DrawableFactory.createSegmentsForBar(
  bar: EventAddress,
  scoreQuery: ScoreQuery
): Iterable<Pair<EventAddress, SegmentArea>> {

  if (scoreQuery.isRepeatBar(bar)){
    return listOf()
  }
  var events = scoreQuery.getEventsForBar(EventType.DURATION, bar).plus(
    scoreQuery.getEventsForBar(EventType.PLACE_HOLDER, bar)
  ).toList()
  events = events.plus(
    scoreQuery.getEventsForStave(
      bar.staveId,
      listOf(EventType.CLEF)
    ).toList().filter { it.first.eventAddress.barNum == bar.barNum && it.first.eventAddress.offset != dZero() })

  return events.groupBy { it.first.eventAddress.offset }.flatMap { (offset, events) ->
    val address = bar.copy(offset = offset)
    val numVoices = scoreQuery.numVoicesAt(address)
    val graceNormal = events.partition { it.first.eventAddress.isGrace }

    val graceSegments = createGraceSegments(graceNormal.first, address)
    val mapped = graceSegments.map { (it.first.graceOffset ?: dZero()) to it.second }.toMap()
    val normalSegments = segmentArea(graceNormal.second.toMap(), address, numVoices, mapped)?.let {
      listOf(Pair(address, it))
    } ?: listOf()
    normalSegments.plus(graceSegments)
  }
}

private fun DrawableFactory.createGraceSegments(
  eventList: EventList,
  eventAddress: EventAddress
): Iterable<Pair<EventAddress, SegmentArea>> {
  return eventList.groupBy { it.first.eventAddress.graceOffset }
    .mapNotNull { (graceOffset, events) ->
      val address = eventAddress.copy(graceOffset = graceOffset)
      segmentArea(events.toMap(), address, 1)?.let {
        Pair(address, it)
      }
    }
}


private fun <T> createStaveLookup(segmentLookup: Lookup<T>): Map<StaveId, Lookup<T>> {
  return segmentLookup.toList().groupBy { it.first.staveId }.map {
    it.key to it.value.toMap()
  }.toMap()
}


private fun  createBarLookup(segmentLookup: Lookup<SegmentGeography>, numBars: Int,
existing: AreaDirectory?, changedBars: Iterable<Int>?): Map<Int, Lookup<SegmentGeography>> {
  val grouped = segmentLookup.toList().groupBy { it.first.barNum }

  return (1..numBars).fold(hashMapOf()) { map, barNum ->
    val existingMap = changedBars?.let {
      if (changedBars.contains(barNum)) null else existing?.segmentGeogBarLookup?.get(barNum)
    }

    val subMap = existingMap ?: grouped[barNum]?.toMap() ?: mapOf()
    map[barNum] = subMap
    map
  }
}



