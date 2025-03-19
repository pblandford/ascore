package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.*

data class ChordDecoration<T>(
  val up: Boolean = false,
  val items: Iterable<T> = listOf(),
  val shift: Coord = Coord()
)

internal interface ChordDecorationSubAdder<T> : BaseSubAdder {
  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return score.getEvent(EventType.DURATION, eventAddress).ifNullRestore(score) { ev ->
      score.doAdd<T>(eventAddress, destination, ev, params)
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
      EventParam.HARD_START -> {
        (value as? Coord).ifNullRestore(score) { coord ->
          score.setDecoration<T>(destination, eventAddress) {
            copy(shift = coord)
          }
        }
      }
      else -> {
        return score.getEvent(EventType.DURATION, eventAddress).ifNullRestore(score) { ev ->
          score.doAdd<T>(eventAddress, destination, ev, paramMapOf(param to value))
        }
      }
    }
  }

  fun <T> Score.setDecoration(
    destination: EventDestination,
    eventAddress: EventAddress, func: ChordDecoration<T>.() -> ChordDecoration<T>
  ): ScoreResult {
    return getEvent(EventType.DURATION, eventAddress)?.getParam<ChordDecoration<T>>(getParam())
      .ifNullRestore(this) { decoration ->
        DurationSubAdder.setParam(
          this,
          destination,
          EventType.DURATION,
          getParam(),
          decoration.func(),
          eventAddress
        )
      }
  }

  private fun <T> Score.doAdd(
    eventAddress: EventAddress,
    destination: EventDestination,
    durationEvent: Event,
    params: ParamMap
  ): ScoreResult {
    return if (durationEvent.subType == DurationType.CHORD) {
      if (isUnique()) {
        addUnique(params)
      } else {
        addNonUnique<T>(params, durationEvent)
      }.then { dec ->
        DurationSubAdder.setParam(
          this,
          destination,
          EventType.DURATION,
          getParam(),
          dec,
          eventAddress
        )
      }
    } else {
      Right(this)
    }
  }

  private fun <T> addUnique(params: ParamMap): AnyResult<ChordDecoration<T>> {
    return getParamVal(params)?.let { subtype ->
      val up = params.isTrue(EventParam.IS_UP)
      Right(ChordDecoration(up, listOf(subtype as T)))
    } ?:
    Left(Error("Could not get decoration value"))
  }

  private fun <T> addNonUnique(
    params: ParamMap,
    durationEvent: Event
  ): AnyResult<ChordDecoration<T>> {
    val cd = getParamVal(params)?.let { paramVal ->

      val up = params.isTrue(EventParam.IS_UP)
      when (paramVal) {
        is Iterable<*> -> ChordDecoration(up, paramVal.toList() as List<T>)
        else -> {
          val existing =
            durationEvent.getParam<ChordDecoration<T>>(getParam()) ?: ChordDecoration(up)
          if (existing.items.contains(paramVal as T)) {
            if (existing.up == up) {
              existing
            } else {
              ChordDecoration(up, existing.items)
            }
          } else {
            ChordDecoration(up, existing.items.plus(paramVal))
          }
        }
      }
    }
    return cd?.let { Right(it) } ?: Left(Error("Could not get decoration value"))
  }

  fun getParam(): EventParam
  fun getParamVal(params: ParamMap): Any?
  fun isUnique(): Boolean = true

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    if (score.getEvent(EventType.DURATION, eventAddress) == null ||
        score.getParam<DurationType>(EventType.DURATION, EventParam.TYPE, eventAddress) !=
        DurationType.CHORD) {
      return score.ok()
    }

    return GenericSubAdder.setParam(
      score,
      destination,
      EventType.DURATION,
      getParam(),
      null,
      eventAddress
    ).failureIsNoop(score)
  }

  override fun deleteEventRange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    return score.getEvents(EventType.DURATION, eventAddress, endAddress)
      .ifNullRestore(score) { events ->
        score.fold(events.toList()) { (k, v) ->
          this@ChordDecorationSubAdder.deleteEvent(
            this,
            destination,
            eventType,
            paramMapOf(),
            k.eventAddress
          )
        }
      }
  }
}