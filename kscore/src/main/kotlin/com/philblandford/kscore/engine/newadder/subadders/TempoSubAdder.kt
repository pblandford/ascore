package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.types.*

internal object TempoSubAdder : NewSubAdder {


  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    if (params.g<Int>(EventParam.BPM) ?: 0 <= 0) {
      return score.ok()
    }
    return super.addEvent(score, destination, eventType, params, eventAddress)
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return if (eventAddress.barNum == 1) {
      score.getEvent(EventType.TEMPO, eventAddress)?.let { existing ->
        val hiddenTempo = existing.addParam(EventParam.HIDDEN, true)
        super.addEvent(score, destination, EventType.TEMPO, hiddenTempo.params, eventAddress)
      } ?: Left(NotFound("No tempo found at $eventAddress"))
    } else {
      super.deleteEvent(score, destination, eventType, params, eventAddress)
    }
  }
}