package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.newadder.EventDestination
import com.philblandford.kscore.engine.newadder.NewSubAdder
import com.philblandford.kscore.engine.newadder.ScoreResult
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.StaveId

object LayoutSubAdder : NewSubAdder {

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