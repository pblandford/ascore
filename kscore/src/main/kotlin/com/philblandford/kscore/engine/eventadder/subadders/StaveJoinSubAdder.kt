package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.*

object StaveJoinSubAdder : LineSubAdderIf {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return params.g<EventAddress>(EventParam.END)?.let { end ->
      val (sortedStart, sortedEnd) = sortStaves(eventAddress, end)
      val numStaves = sortedEnd.staveId.main - sortedStart.staveId.main + 1
      val newParams = params.plus(EventParam.NUMBER to numStaves)
      var map = score.eventMap.adjustExisting(sortedStart.staveId.main, numStaves)
      val address = sortedStart.copy(barNum = 0, offset = dZero(), staveId = StaveId(sortedStart.staveId.main, 0), voice = 0)
      map = map.putEvent(address, Event(EventType.STAVE_JOIN, newParams))
      Right(Score(score.parts, map, score.beamDirectory))
    } ?: Left(ParamsMissing(listOf(EventParam.END)))
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    val eventMap = score.eventMap.deleteEvent(eventAddress, eventType)
    return Right(score.replaceSelf(eventMap))
  }

  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    return Right(score)
  }

  private fun sortStaves(start:EventAddress, end:EventAddress):Pair<EventAddress, EventAddress>{
    return if (start.staveId > end.staveId) {
      end to start
    } else {
      start to end
    }
  }

  private fun previousOverlaps(eventAddress: EventAddress, partNum: Int, num: Int): Boolean {
    return eventAddress.staveId.main < partNum && eventAddress.staveId.main + num - 1 >= partNum
  }

  private fun nextOverlaps(eventAddress: EventAddress, partNum: Int, numParts: Int): Boolean {
    return partNum < eventAddress.staveId.main && partNum + numParts > eventAddress.staveId.main
  }

  private fun EventMap.adjustExisting(partNum: Int, numParts: Int): EventMap {
    return getEvents(EventType.STAVE_JOIN)?.toList()?.fold(this) { em, (key, event) ->
      val num = event.getInt(EventParam.NUMBER)
      when {
        previousOverlaps(key.eventAddress, partNum, num) -> {
          val num = partNum - key.eventAddress.staveId.main
          if (num < 2) {
            em.deleteEvent(key.eventAddress, EventType.STAVE_JOIN)
          } else {
            em.putEvent(
              key.eventAddress,
              event.addParam(EventParam.NUMBER, partNum - key.eventAddress.staveId.main)
            )
          }
        }
        nextOverlaps(key.eventAddress, partNum, numParts) -> {
          val thisParts = event.getInt(EventParam.NUMBER)
          em.deleteEvent(key.eventAddress, EventType.STAVE_JOIN).putEvent(
            key.eventAddress.copy(staveId = StaveId(partNum + numParts, 0)),
            event.addParam(EventParam.NUMBER, thisParts - (key.eventAddress.staveId.main - partNum))
          )
        }
        else -> {
          em
        }
      }
    } ?: this
  }
}