package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.*

internal fun collectEventsForDecoration(
  scoreQuery: ScoreQuery,
  staveAddress: EventAddress,
  endBar: Int,
  isTopPart:Boolean
): EventHash {

  var events = scoreQuery.getEventsForStave(
    staveAddress.staveId,
    decoratorTypes.plus(EventType.REPEAT_BAR), staveAddress.dec(),
    staveAddress.copy(barNum = endBar, offset = DURATION_WILD)
  )
  events = events.plus(getSystemEvents(staveAddress.staveId, scoreQuery, isTopPart))
  events = events.plus(getPartEvents(staveAddress.staveId, scoreQuery))
  return events
}

private fun getSystemEvents(staveId: StaveId, scoreQuery: ScoreQuery, isTopPart: Boolean): EventHash {
  val events = if ((staveId.sub == 1 && isTopPart) ||
    scoreQuery.singlePartMode() && staveId.sub == 1) {
    scoreQuery.getSystemEvents()
  } else eventHashOf()
  val fermataEvents = scoreQuery.getEvents(EventType.FERMATA) ?: eventHashOf()
  return events.plus(fermataEvents)
}

private fun getPartEvents(staveId: StaveId, scoreQuery: ScoreQuery): EventHash {
  val numStaves = scoreQuery.numStaves(staveId.main)
  return if (staveId.sub == numStaves) {
    scoreQuery.getEventsForPart(staveId.main).map {
      it.key.copy(eventAddress = it.key.eventAddress.copy(staveId = staveId)) to it.value
    }.toMap()
  } else {
    eventHashOf()
  }
}

