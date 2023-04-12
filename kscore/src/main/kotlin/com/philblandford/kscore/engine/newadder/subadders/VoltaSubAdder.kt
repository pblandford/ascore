package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.types.*

internal object VoltaSubAdder : NewSubAdder {

  override fun addEventRange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    return addEvent(score, destination, eventType, params.plus(EventParam.END to endAddress), eventAddress)
  }

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return getEnd(params, eventAddress)?.let { end ->
      val numBars = end.barNum - eventAddress.barNum + 1
      val newParams = params.minus(EventParam.END).plus(EventParam.NUM_BARS to numBars)
      val endParams = params.plus(EventParam.END to true).plus(EventParam.NUM_BARS to numBars)
      var newMap = clearExisting(score.eventMap, eventAddress.barNum, end.barNum)
      val newEvent = Event(EventType.VOLTA, newParams)
      val endEvent = Event(EventType.VOLTA, endParams)
      newMap = newMap.putEvent(adjustAddress(eventAddress, newEvent), newEvent)

      newMap = newMap.putEvent(
        adjustAddress(
          end.copy(
            staveId = eventAddress.staveId,
            offset = dZero(),
            graceOffset = null
          ), endEvent
        ),
        endEvent
      )

      Right(score.replaceSelf(newMap) as Score)
    } ?: Left(NotFound("Could not get end of volta"))
  }

  private fun getEnd(params: ParamMap, eventAddress: EventAddress): EventAddress? {
    return when (val end = params.g<Any>(EventParam.END)) {
      is EventAddress -> end
      else -> eventAddress.inc((params.g<Int>(EventParam.NUM_BARS) ?: 1) - 1)
    }
  }

  private fun adjustAddress(eventAddress: EventAddress, event: Event): EventAddress {
    return eventAddress.staveless().startBar()
  }

  private fun clearExisting(eventMap: EventMap, start: Int, end: Int): EventMap {
    return eventMap.getEvents(EventType.VOLTA)?.toList()?.fold(eventMap) { em, (k, v) ->
      val endBar = if (v.isTrue(EventParam.END)) k.eventAddress.barNum
      else k.eventAddress.barNum + v.getInt(EventParam.NUM_BARS, 1) - 1
      if ((k.eventAddress.barNum in start..end) || (!v.isTrue(EventParam.END) && endBar in start..end)
      ) {
        em.deleteEvent(k.eventAddress, v.eventType).deleteEvent(
          k.eventAddress.copy(barNum = endBar),
          v.eventType
        )
      } else em

    } ?: eventMap
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {


    val numBars = score.getParam<Int>(eventType, EventParam.NUM_BARS, eventAddress) ?: 1
    val endBar = eventAddress.barNum + numBars - 1
    val newMap = score.eventMap.deleteEvent(eventAddress, eventType)
      .deleteEvent(eventAddress.copy(barNum = endBar), eventType)

    return Right(score.replaceSelf(newMap) as Score)
  }

  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    val address = if (param == EventParam.HARD_START) score.getFirstInGroup(eventAddress) else eventAddress
    return super.setParam(score, destination, eventType, param, value, address)
  }

  private fun Score.getFirstInGroup(eventAddress: EventAddress):EventAddress {
    var address:EventAddress = eventAddress

    while (address.barNum > 0) {
      getEvent(EventType.VOLTA, address-1)?.let { previous ->
        address -= previous.getInt(EventParam.NUM_BARS, 1)
      } ?: run { return address }
    }
    return address
  }
}
