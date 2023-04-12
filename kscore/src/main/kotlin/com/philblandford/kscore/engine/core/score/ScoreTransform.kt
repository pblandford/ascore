package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.types.*


private fun doScoreOp(
  op: () -> ScoreResult
): Score? {

  return when (val res = op()) {
    is Right -> res.r
    is Warning -> res.r
    is AbortNoError -> null
    is Left -> throw Exception(res.l.message, res.l.exception)
  }
}

fun Score.addEvent(
  event: Event,
  eventAddress: EventAddress,
  endAddress: EventAddress? = null
): Score? {
  return doScoreOp {
    if (endAddress != null) {
      NewEventAdder.addEventRange(
        this,
        event.eventType,
        event.params,
        eventAddress,
        endAddress
      )
    } else {
      NewEventAdder.addEvent(this, event.eventType, event.params, eventAddress)
    }
  }
}

fun Score.deleteEvent(
  eventType: EventType, params: ParamMap, eventAddress: EventAddress,
  endAddress: EventAddress?
): Score? {
  return doScoreOp {
    if (endAddress != null) {
      NewEventAdder.deleteEventRange(this, eventType, eventAddress, endAddress)
    } else {
      NewEventAdder.deleteEvent(this, eventType, params, eventAddress)
    }
  }
}

fun <T> Score.setParam(
  eventType: EventType,
  param: EventParam,
  value: T,
  eventAddress: EventAddress,
  end: EventAddress? = null
): Score? {
  return doScoreOp {
    end?.let {
      NewEventAdder.setParamRange(this, eventType, param, value, eventAddress, end)
    } ?: run {
      NewEventAdder.setParam(this, eventType, param, value, eventAddress)
    }
  }
}

