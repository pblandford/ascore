package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap

interface RangeSubAdder : BaseEventAdder {

  override fun addEventRange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    return super.addEventRange(score, destination, eventType, params, eventAddress, endAddress)
  }

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return if (score.getEventAt(eventType, eventAddress)?.second?.params == params) {
      Warning(HarmlessFailure("Identical event $params already here"), score)
    } else {
      super.addEvent(score, destination, eventType, params, eventAddress)
    }
  }

}

