package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.newadder.EventDestination
import com.philblandford.kscore.engine.newadder.NewSubAdder
import com.philblandford.kscore.engine.newadder.ScoreResult
import com.philblandford.kscore.engine.types.*

object LyricSubAdder : NewSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    val num = params.g<Int>(EventParam.NUMBER) ?: 1
    val address = eventAddress.copy(id = num)
    val newParams = params.g<LyricType>(EventParam.TYPE)?.let { params } ?: params.plus(EventParam.TYPE to LyricType.ALL)
    return super.addEvent(score, destination, eventType, newParams, address)
  }

}