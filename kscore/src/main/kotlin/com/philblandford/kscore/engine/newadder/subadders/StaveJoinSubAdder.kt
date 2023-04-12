package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.types.*
import javax.sound.sampled.Line

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
      val newParams = params.plus(EventParam.NUMBER to numStaves).minus(EventParam.END)
      var map = score.eventMap.adjustExisting(sortedStart.staveId.main, numStaves)
      val address = sortedStart.copy(barNum = 0, offset = dZero(), staveId = StaveId(sortedStart.staveId.main, 0))
      map = map.putEvent(address, Event(EventType.STAVE_JOIN, newParams))
      Right(Score(score.parts, map))
    } ?: Left(ParamsMissing(listOf(EventParam.END)))
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
    return partNum < eventAddress.staveId.main && partNum + numParts >= eventAddress.staveId.main
  }

  private fun EventMap.adjustExisting(partNum: Int, numParts: Int): EventMap {
    return getEvents(EventType.STAVE_JOIN)?.toList()?.fold(this) { em, (key, event) ->
      val num = event.getInt(EventParam.NUMBER)
      when {
        previousOverlaps(key.eventAddress, partNum, num) -> {
          em.putEvent(
            key.eventAddress,
            event.addParam(EventParam.NUMBER, partNum - key.eventAddress.staveId.main)
          )
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