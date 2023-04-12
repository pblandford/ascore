package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Bar
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.map.emptyEventMap
import com.philblandford.kscore.engine.map.eventMapOf
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.types.*

object RepeatBarSubAdder : NewSubAdder {

  override fun addEvent(
      score: Score,
      destination: EventDestination,
      eventType: EventType,
      params: ParamMap,
      eventAddress: EventAddress
  ): ScoreResult {

    if (score.getParam<Int>(EventType.REPEAT_BAR, EventParam.NUMBER, eventAddress - 1) == 2) {
      return Warning("Not adding to two bar repeat", score)
    }
    return params.g<Int>(EventParam.NUMBER).ifNullError("Require number parameter") { number ->
      super.addEvent(score, destination, eventType, params, eventAddress.startBar().voiceIdless())
          .then { it.deleteExisting(number, eventAddress) }
          .then { it.removeBarEvents(eventAddress, number) }
    }
  }

  override fun addEventRange(
      score: Score,
      destination: EventDestination,
      eventType: EventType,
      params: ParamMap,
      eventAddress: EventAddress,
      endAddress: EventAddress
  ): ScoreResult {
    val bars = score.getStaveRange(eventAddress.staveId, endAddress.staveId).flatMap { staveId ->
      (eventAddress.barNum..endAddress.barNum).map { bar ->
        EventAddress(bar, staveId = staveId)
      }
    }
    return score.fold(bars) { address ->
      addEvent(this, destination, eventType, params, address)
    }
  }

  private fun Score.deleteExisting(number: Int, eventAddress: EventAddress): ScoreResult {
    return if (number == 2) {
      super.deleteEvent(this, staveDestination, EventType.REPEAT_BAR, paramMapOf(),
          eventAddress.inc())
    } else ok()
  }

  private fun Score.removeBarEvents(eventAddress: EventAddress, number: Int): ScoreResult {
    val endBar = if (number == 1) eventAddress.barNum else eventAddress.barNum + 1
    return transformBars(eventAddress.barNum, endBar) { _, _, _, staveId ->
      if (staveId == eventAddress.staveId) {
        Bar(timeSignature,
            eventMap = eventMapOf(eventMap.getEvents(EventType.HARMONY) ?: mapOf())).ok()
      } else ok()
    }
  }
}