package com.philblandford.kscore.sound

import com.philblandford.kscore.engine.map.RegionMap
import com.philblandford.kscore.engine.map.regionMap
import com.philblandford.kscore.engine.newadder.notNull
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.DynamicType.*

private val DEFAULT_VELOCITY = 100

internal class MidiDynamicGetter(private val scoreQuery: ScoreQuery) {

  val maps = getDynamicMaps(scoreQuery)

  fun getVelocity(eventAddress: EventAddress): Int {
    return getDynamicEvent(eventAddress)?.let { event ->
      velocities[event.subType as? DynamicType] ?: DEFAULT_VELOCITY
    } ?: DEFAULT_VELOCITY
  }

  private fun getEventFromMaps(eventAddress: EventAddress): Pair<EventAddress, Event>? {
    return maps[eventAddress.staveId]?.floorEntry(eventAddress.voiceIdless())
      ?.let { it.key to it.value }
  }

  private fun getDynamicMaps(scoreQuery: ScoreQuery): Map<StaveId, RegionMap> {
    return scoreQuery.getAllStaves(true).map { staveId ->
      val dynamics = scoreQuery.getEventsForStave(staveId, listOf(EventType.DYNAMIC))
      val addresses = scoreQuery.getEventsForStave(staveId, listOf(EventType.DURATION))
        .keys.map { it.eventAddress.voiceIdless() }
      staveId to regionMap(dynamics, EventType.DYNAMIC, addresses) { ea, e ->
        e.addParam(EventParam.START, ea)
      }
    }.toMap()
  }

  private fun getDynamicEvent(eventAddress: EventAddress): Event? {
    val numStaves = scoreQuery.numStaves(eventAddress.staveId.main)
    return if (numStaves == 1) {
      getEventFromMaps(eventAddress)?.second
    } else if (numStaves > 0) {
      val all = (1..numStaves).mapNotNull { sub ->
        getEventFromMaps(
          eventAddress.copy(
            staveId = StaveId(
              eventAddress.staveId.main,
              sub
            )
          )
        )
      }
      val grouped = all.groupBy {
        it.second.getParam<EventAddress>(EventParam.START)?.barNum ?: it.first.barNum
      }.toList().sortedBy { it.first }
      grouped.lastOrNull()?.let { last ->
        if (last.second.size > 1) {
          return getEventFromMaps(eventAddress)?.second
        }
      }
      all.maxByOrNull { it.first.horizontal }?.second
    } else null
  }
}

internal val velocities = mapOf(
  MOLTO_FORTISSIMO to 127,
  FORTISSIMO to 115,
  FORTE to 105,
  MEZZO_FORTE to 100,
  MEZZO_PIANO to 80,
  PIANO to 70,
  PIANISSIMO to 50,
  MOLTO_PIANISSIMO to 40,
  SFORZANDISSMO to 115,
  SFORZANDO to 105,
  SFORZANDO_PIANO to 105,
  FORTE_PIANO to 105
)

