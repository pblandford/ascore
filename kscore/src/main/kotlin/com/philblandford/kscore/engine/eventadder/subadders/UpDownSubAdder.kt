package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.*

interface UpDownSubAdderIf : MoveableSubAdderIf {
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
    return if (param == EventParam.IS_UP) {
      val adjustedAddress = eventAddress.copy(id = if (value == true) 0 else 1)
       score.getEvent(eventType, eventAddress)?.let { existing ->
        deleteEvent(score, destination, eventType, paramMapOf(), eventAddress).then {
          addEvent(
            it,
            destination,
            eventType,
            existing.params.plus(param to value),
            adjustedAddress
          )
        }
      } ?: Left(NotFound("UpDown event not found at $eventAddress"))
    } else {
      super.setParam(score, destination, eventType, param, value, eventAddress)
    }
  }
}

