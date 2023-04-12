package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.newadder.EventDestination
import com.philblandford.kscore.engine.newadder.NewSubAdder
import com.philblandford.kscore.engine.newadder.ScoreResult
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.StaveId

object BreakSubAdder : NewSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return if (!score.singlePartMode()) {
      super.addEvent(score, destination, eventType, params, eventAddress)
    } else {
      val address = eventAddress.copy(staveId = StaveId(score.selectedPart(), 0))
      super.addEvent(score, EventDestination(listOf(ScoreLevelType.PART)), eventType, params, address)
    }
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return if (!score.singlePartMode()) {
      super.deleteEvent(score, destination, eventType, params, eventAddress)
    } else {
      val address = eventAddress.copy(staveId = StaveId(score.selectedPart(), 0))
      super.deleteEvent(score, destination, eventType, params, address)
    }
  }
}