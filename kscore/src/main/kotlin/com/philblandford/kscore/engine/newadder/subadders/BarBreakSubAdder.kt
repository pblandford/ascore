package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.newadder.util.addEventToLevel
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*

object BarBreakSubAdder : NewSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return score.getNext(eventAddress)
      .then { next ->
        score.fold(score.getPlaceHolderPositions(eventAddress, next)) { div ->
          addEventToLevel(
            eventAddress.copy(offset = div), ScoreLevelType.BAR,
            Event(EventType.PLACE_HOLDER, paramMapOf(EventParam.REAL_DURATION to div))
          ).then {
            it.addEventToLevel(
              eventAddress.copy(offset = dZero()), ScoreLevelType.BAR,
              Event(EventType.PLACE_HOLDER, paramMapOf(EventParam.REAL_DURATION to div))
            )
          }
        }
      }
  }

  private fun Score.getPlaceHolderPositions(eventAddress: EventAddress, next:Offset):List<Offset> {
    val timeSignature = getTimeSignature(eventAddress) ?: TimeSignature(4,4)
    return if (next - eventAddress.offset == timeSignature.duration) {
      timeSignature.getDivisisions()
    } else {
      val duration = next - eventAddress.offset
      listOf(eventAddress.offset + (duration / 2))
    }
  }

  private fun TimeSignature.getDivisisions() : List<Offset> {
    return if ((numerator % 2) != 0) {
      val offset = duration / numerator
      (0 until numerator).map { offset * it }
    } else {
      listOf(duration / 2)
    }
  }



  private fun Score.getNext(
    eventAddress: EventAddress
  ): AnyResult<Offset> {
    return getBar(eventAddress)?.let { bar ->
      val existing = bar.eventMap.getEvents(EventType.PLACE_HOLDER)?.toList()
        ?.sortedBy { it.first.eventAddress.offset }
        ?.dropWhile { it.first.eventAddress.offset <= eventAddress.offset }

      val res = existing?.minByOrNull { it.first.eventAddress.offset }?.first?.eventAddress?.offset
        ?: getTimeSignature(eventAddress)?.duration ?: semibreve()
      Right(res)
    } ?: Left(NotFound("Bar not found at $eventAddress"))
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return GenericSubAdder.deleteEventRange(score, destination, EventType.PLACE_HOLDER, eventAddress.startBar(),
    eventAddress.copy(offset = dWild()))
      .then {
        GenericSubAdder.deleteEventRange(it, destination, EventType.HARMONY, eventAddress.startBar(),
          eventAddress.copy(offset = dWild()))
      }
  }

}