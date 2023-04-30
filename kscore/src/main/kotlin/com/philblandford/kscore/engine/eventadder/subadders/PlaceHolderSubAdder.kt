package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.duration.semibreve
import com.philblandford.kscore.engine.duration.subtractC
import com.philblandford.kscore.engine.eventadder.EventDestination
import com.philblandford.kscore.engine.eventadder.BaseEventAdder
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.eventadder.util.addEventToLevel
import com.philblandford.kscore.engine.types.*

object PlaceHolderSubAdder : BaseEventAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    val next =
      score.collateEvents(listOf(EventType.DURATION, EventType.PLACE_HOLDER))?.let { events ->
        events.toList()
          .takeLastWhile { it.first.eventAddress.offset > eventAddress.offset }
          .firstOrNull()?.first?.eventAddress?.offset
      } ?: score.getTimeSignature(eventAddress)?.duration ?: semibreve()

    val duration = next.subtractC(eventAddress.offset)
    val newEvent = Event(eventType, params.plus(EventParam.REAL_DURATION to duration))
    return score.addEventToLevel(eventAddress, ScoreLevelType.BAR, newEvent)
  }

}