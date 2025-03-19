package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.eventadder.EventDestination
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.types.*

internal object TremoloSubAdder : ChordDecorationSubAdder<Duration> {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return if (params[EventParam.TREMOLO_BEATS] == dZero()) {
      super.deleteEvent(score, destination, eventType, params, eventAddress)
    } else {
      super.addEvent(score, destination, eventType, params, eventAddress)
    }
  }

  override fun getParam(): EventParam {
    return EventParam.TREMOLO_BEATS
  }

  override fun getParamVal(params:ParamMap): Any? {
    return params.g<Duration>(EventParam.TREMOLO_BEATS)
  }

  override fun <U> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: U,
    eventAddress: EventAddress
  ): ScoreResult {
    return if (value == dZero()) {
      super.deleteEvent(score, destination, eventType, paramMapOf(), eventAddress)
    } else {
      super.setParam(score, destination, eventType, param, value, eventAddress)
    }
  }
}