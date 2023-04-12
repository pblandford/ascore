package com.philblandford.kscore.sound

import com.philblandford.kscore.engine.map.RegionMap
import com.philblandford.kscore.engine.map.regionMap
import com.philblandford.kscore.engine.types.*

internal class MidiLongTrillGetter(scoreQuery: ScoreQuery) {

  private val maps = getLongTrillMaps(scoreQuery)

  fun longTrillActive(eventAddress: EventAddress):Event? {
    return maps[eventAddress.staveId]?.let { map ->
      map[eventAddress.voiceIdless()]
    }
  }

  private fun getLongTrillMaps(scoreQuery: ScoreQuery): Map<StaveId, RegionMap> {
    return scoreQuery.getAllStaves(true).map { staveId ->
      val longTrills = scoreQuery.getEventsForStave(staveId, listOf(EventType.LONG_TRILL))
      val addresses = scoreQuery.getEventsForStave(staveId, listOf(EventType.DURATION))
        .keys.map { it.eventAddress.voiceIdless() }.distinct()
      staveId to regionMap(longTrills, EventType.LONG_TRILL, addresses)
    }.toMap()
  }
}


