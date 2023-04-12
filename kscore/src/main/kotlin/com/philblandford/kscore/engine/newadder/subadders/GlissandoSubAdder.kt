package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap

object GlissandoSubAdder : LineSubAdderIf {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return super.addEvent(score, destination, eventType, params, eventAddress).otherwise {
      score.getNextStaveSegment(eventAddress)?.let {
        val newParams = params.plus(EventParam.END to it)
        super.addEvent(score, destination, eventType, newParams, eventAddress)
      } ?: Left(NotFound("Could not get next segment"))
    }
  }

  override fun adjustAddress(eventAddress: EventAddress, params: ParamMap): EventAddress {
    return eventAddress
  }
}