package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.newadder.EventDestination
import com.philblandford.kscore.engine.newadder.ScoreResult
import com.philblandford.kscore.engine.newadder.ifNullRestore
import com.philblandford.kscore.engine.newadder.ok
import com.philblandford.kscore.engine.types.*

internal object OrnamentSubAdder : ChordDecorationSubAdder<Ornament> {

  override fun getParam(): EventParam {
    return EventParam.ORNAMENT
  }

  override fun getParamVal(params: ParamMap): Any? {
    return params.g<OrnamentType>(EventParam.TYPE)?.let { type ->
      return Ornament(
        type,
        params.g<Accidental>(EventParam.ACCIDENTAL_ABOVE),
        params.g<Accidental>(EventParam.ACCIDENTAL_BELOW)
      )
    }
  }

  override fun <U> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: U,
    eventAddress: EventAddress
  ): ScoreResult {
    return when (param) {
      EventParam.TYPE -> {
        (value as? OrnamentType).ifNullRestore(score) { type ->
          score.setDecoration<Ornament>(destination, eventAddress) {
            copy(items = items.map { it.copy(ornamentType = type) })
          }
        }
      }
      else -> score.ok()
    }
  }
}