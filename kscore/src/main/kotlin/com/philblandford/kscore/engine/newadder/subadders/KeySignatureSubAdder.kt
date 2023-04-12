package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.newadder.util.deleteEventFromMap
import com.philblandford.kscore.engine.newadder.util.nextEvent
import com.philblandford.kscore.engine.newadder.util.previousEvent
import com.philblandford.kscore.engine.newadder.util.setAccidentals
import com.philblandford.kscore.engine.types.*

internal object KeySignatureSubAdder : NewSubAdder {
  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    val stripped = ez(eventAddress.barNum)
    if (score.previousEvent(eventType, stripped)?.second?.params == params) {
      return Warning(HarmlessFailure("Key already present"), score)
    }
    return super.addEvent(score, destination, eventType, params, stripped)
      .then { it.removeLater(eventType, params, stripped) }
      .then { it.replaceAccidentals(stripped) }
  }

  private fun Score.removeLater(
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    nextEvent(eventType, ez(eventAddress.barNum))?.let { (ea, ev) ->
      if (ev.params == params) {
        return Right(deleteEventFromMap(eventType, ea))
      }
    }
    return Right(this)
  }

  private fun Score.replaceAccidentals(eventAddress: EventAddress): ScoreResult {
    val endBar = getEvents(EventType.KEY_SIGNATURE)?.toList()
      ?.takeLastWhile { it.first.eventAddress.barNum > eventAddress.barNum }
      ?.firstOrNull()?.first?.eventAddress?.barNum
      ?: numBars

    return setAccidentals(eventAddress, ez(endBar))
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return if (eventAddress.barNum == 1) {
      setParam(score, destination, eventType, EventParam.SHARPS, 0, eventAddress)
    } else {
      super.deleteEvent(score, destination, eventType, params, eventAddress)
    }
  }
}