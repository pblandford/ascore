package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.types.*

interface UpDownSubAdderIf : NewSubAdder {
  fun adjustAddress(eventAddress: EventAddress, params:ParamMap): EventAddress {
    val id = params.g<Boolean>(EventParam.IS_UP)?.let { up ->
      if (up) 0 else 1
    } ?: 0
    return eventAddress.copy(id = id)
  }

  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    val adjustedAddress = if (param == EventParam.IS_UP) {
      eventAddress.copy(id = if (value == true) 0 else 1)
    } else {
      eventAddress
    }
    return score.getEvent(eventType, eventAddress)?.let { existing ->
      deleteEvent(score, destination, eventType, paramMapOf(), eventAddress).then {
        addEvent(it, destination, eventType, existing.params.plus(param to value), adjustedAddress)
      }
    } ?: Left(NotFound("Line event not found at $eventAddress"))
  }
}

