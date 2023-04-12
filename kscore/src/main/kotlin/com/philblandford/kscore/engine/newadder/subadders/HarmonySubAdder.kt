package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.pitch.harmony
import com.philblandford.kscore.engine.types.*

object HarmonySubAdder : NewSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return harmony(Event(EventType.HARMONY, params))?.toEvent()?.let {
      super.addEvent(score, destination, eventType, params.minus(EventParam.TEXT).plus(it.params), eventAddress)
        .then { MarkerSubAdder.addEvent(it, destination, eventType, params, eventAddress) }
    } ?: Left(ParamsMissing(listOf(EventParam.TONE)))
  }

}