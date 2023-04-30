package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.BaseEventAdder
import com.philblandford.kscore.engine.eventadder.EventDestination
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

object MoveableSubAdder : MoveableSubAdderIf

interface MoveableSubAdderIf : BaseEventAdder {

  @Suppress("IMPLICIT_CAST_TO_ANY")
  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {

    val adjustedValue = when (param) {
      EventParam.HARD_START, EventParam.HARD_MID, EventParam.HARD_END -> {
        (value as? Coord)?.let { adjust ->
        score.getParam<Coord>(eventType, param, eventAddress)?.let { existing ->
            existing + adjust
          } ?: adjust
        } ?: Coord()
      }
      else -> {
        value
      }
    }

    return super.setParam(score, destination, eventType, param, adjustedValue, eventAddress)
  }
}