package com.philblandford.kscore.engine.core.areadirectory.text

import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.PAUSE_WIDTH
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.Lookup
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.kscore.engine.types.ez

internal fun createFermataWidths(scoreQuery: ScoreQuery):Lookup<Int> {

  val events = scoreQuery.getEvents(EventType.FERMATA) ?: eventHashOf()
  val grouped = events.toList().groupBy { Pair(it.first.eventAddress.barNum,
    it.first.eventAddress.offset) }
  return grouped.mapNotNull { (k, _) ->
     ez(k.first, k.second) to fermataWidth + BLOCK_HEIGHT*2
  }.toMap()
}

private fun getWidth(eventType: EventType):Int {
  return when (eventType) {
    EventType.FERMATA -> fermataWidth + BLOCK_HEIGHT*2
    EventType.PAUSE -> PAUSE_WIDTH
    else -> 0
  }
}

private val fermataWidth = BLOCK_HEIGHT*2

