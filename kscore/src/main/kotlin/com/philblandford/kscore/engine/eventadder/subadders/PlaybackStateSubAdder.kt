package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.*

object PlaybackStateSubAdder : BaseSubAdder {

  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.updateOthers(destination, eventType, param, value, eventAddress).then {
      super.setParam(it, destination, eventType, param, value, eventAddress)
    }
  }

  private fun <T> Score.updateOthers(
    destination: EventDestination,
    eventType: EventType,
    eventParam: EventParam, value: T, eventAddress: EventAddress
  ): ScoreResult {
    return fold(0..numParts) { p ->
      val partAddress = eventAddress.copy(staveId = StaveId(p, 0))
      var existing = getEvent(eventType, partAddress)?.params ?: paramMapOf()
      if (eventParam == EventParam.SOLO) {
        val solo = (value as? Boolean) ?: false
        existing =
          if (p == eventAddress.staveId.main) {
            existing.plus(EventParam.MUTE to false).plus(EventParam.SOLO to solo)
          } else {
            existing.plus(EventParam.MUTE to solo).plus(EventParam.SOLO to false)
          }
      }
      if (eventParam == EventParam.MUTE) {
        if (p == eventAddress.staveId.main) {
          existing = existing.plus(EventParam.SOLO to false)
            .plus(EventParam.MUTE to value)
        }
      }

      super.addEvent(
        this,
        destination,
        eventType,
        existing,
        partAddress
      )
    }
  }
}