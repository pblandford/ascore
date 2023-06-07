package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.eventadder.EventDestination
import com.philblandford.kscore.engine.eventadder.BaseSubAdder
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.StaveId

object LayoutSubAdder : BaseSubAdder {

  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    return if (score.singlePartMode()) {
      super.setParam(score, EventDestination(listOf(ScoreLevelType.PART)), eventType, param, value,
        eventAddress.copy(staveId = StaveId(score.selectedPart(), 0)))
    } else {
      super.setParam(score, destination, eventType, param, value, eventAddress)
    }
  }
}