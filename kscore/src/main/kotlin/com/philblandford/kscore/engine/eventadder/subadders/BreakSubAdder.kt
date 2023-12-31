package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.eventadder.EventDestination
import com.philblandford.kscore.engine.eventadder.BaseSubAdder
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.StaveId

object BreakSubAdder : BaseSubAdder {

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
      super.deleteEvent(score, EventDestination(listOf(ScoreLevelType.PART)), eventType, params, address)
    }
  }
}