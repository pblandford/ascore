package com.philblandford.kscore.engine.core.areadirectory.text

import com.philblandford.kscore.engine.core.representation.GLISSANDO_EXTRA
import com.philblandford.kscore.engine.core.representation.PAUSE_WIDTH
import com.philblandford.kscore.engine.types.*

internal fun createSegmentExtensions(scoreQuery: ScoreQuery): Lookup<Int> {

  return scoreQuery.collateEvents(listOf(EventType.GLISSANDO,
    EventType.PAUSE, EventType.SPACE))?.mapNotNull { (key, event) ->
    if (event.isTrue(EventParam.END)) null else key.eventAddress to getWidth(event)
  }?.toMap() ?: mapOf()
}

private fun getWidth(event:Event):Int {
  return when (event.eventType) {
    EventType.GLISSANDO -> GLISSANDO_EXTRA + event.getInt(EventParam.EXTRA_WIDTH)
    EventType.PAUSE -> PAUSE_WIDTH
    EventType.SPACE -> event.getInt(EventParam.AMOUNT)
    else -> 0
  }
}

