package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevel
import com.philblandford.kscore.engine.core.score.Stave
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.eventadder.util.changeSubLevel
import com.philblandford.kscore.engine.eventadder.util.getLevel
import com.philblandford.kscore.engine.eventadder.util.strip
import com.philblandford.kscore.engine.types.*

internal interface LineSubAdderIf : UpDownSubAdderIf {
  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.getLevel(destination, eventAddress)?.let { stave ->
      stave.addLine(score, eventType, params, eventAddress).then { sr ->
        score.changeSubLevel(sr, eventAddress)
      }
    } ?: Left(NotFound("No stave found at $eventAddress"))
  }

  override fun addEventRange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    return addEvent(score, destination, eventType, params.plus(EventParam.END to endAddress),
    eventAddress)
  }

  private fun ScoreLevel.addLine(
    score: Score,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): AnyResult<ScoreLevel> {
    if (params.isTrue(EventParam.END)) {
      return Right(replaceSelf(eventMap.putEvent(eventAddress.stavelessWithId(), Event(eventType, params))))
    }

    val swapped = swapAddresses(eventAddress, params)
    return score.getDurationEnd(
      swapped.second,
      swapped.first
    ).then { (duration, end) ->
      var newParams =
        swapped.second.minus(EventParam.END).plus(EventParam.DURATION to duration)
      end.graceOffset?.let { graceOffset ->
        newParams = newParams.plus(EventParam.GRACE_OFFSET_END to graceOffset)
      }
      val endParams =
        swapped.second.plus(EventParam.END to true).plus(EventParam.DURATION to duration)
      val adjusted = adjustAddress(swapped.first, endParams)
      var newMap = score.adjustExisting(eventMap, adjusted, swapped.first, end, eventType)
      newMap = newMap.putEvent(adjusted.adjustForDestination(), Event(eventType, newParams))
      newMap = newMap.putEvent(
        adjustAddress(end, endParams).adjustForDestination(),
        Event(eventType, endParams)
      )

      Right(replaceSelf(newMap))
    }
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.getLevel(destination, eventAddress)
      .ifNullError("Stave not found at $eventAddress") { level ->
        val mapAddress = eventAddress.strip(level.scoreLevelType, eventType)

        level.eventMap.getEvent(eventType, mapAddress).ifNullRestore(score) { event ->
          level.eventMap.deleteEvent(mapAddress, eventType).ok().then { newMap ->
            event.getParam<Duration>(EventParam.DURATION)
              .ifNullError("Line has no duration") { duration ->
                score.addDuration(mapAddress, duration)
                  .ifNullError("Could not add duration $duration to $eventAddress") { end ->
                    val em = newMap.deleteEvent(end, eventType)
                    val newLevel = level.replaceSelf(em, level.subLevels)
                    score.changeSubLevel(newLevel, eventAddress)
                  }
              }
          }
        }
      }
  }

  fun EventAddress.adjustForDestination() = stavelessWithId()

  private fun swapAddresses(
    eventAddress: EventAddress,
    params: ParamMap
  ): Pair<EventAddress, ParamMap> {
    return when (val end = params[EventParam.END]) {
      is EventAddress -> {
        val both = listOf(eventAddress, end)
        val startAddress = both.minOrNull()!!
        val endAddress = both.maxOrNull()!!
        val newParams = params.plus(EventParam.END to endAddress)
        startAddress to newParams
      }
      else -> eventAddress to params
    }
  }

  private fun Score.getDurationEnd(
    params: ParamMap, eventAddress: EventAddress
  ): AnyResult<Pair<Duration, EventAddress>> {

    return params.g<Duration>(EventParam.DURATION)?.let { duration ->
      addDuration(eventAddress, duration)?.let { end ->
        val withGrace = params.g<Offset>(EventParam.GRACE_OFFSET_END)?.let {
          end.copy(graceOffset = it)
        } ?: end
        Right(Pair(duration, withGrace))
      }
    } ?: params.g<EventAddress>(EventParam.END)?.let { end ->
      getDuration(eventAddress, end)?.let { duration ->
        Right(Pair(duration, end))
      }
    } ?: Left(ParamsMissing(listOf(EventParam.DURATION, EventParam.END)))
  }

  private fun Score.adjustExisting(
    eventMap: EventMap, eventAddress: EventAddress,
    originalAddress: EventAddress,
    endAddress: EventAddress, eventType: EventType
  ): EventMap {
    val newEvents = eventMap.getEvents(eventType)?.let { events ->
      val mutable = events.toMutableMap()
      events.filter { it.key.eventAddress.id == eventAddress.id }.forEach { (key, value) ->
        adjustPreviousOverlap(originalAddress, key, value, mutable)
        adjustNextOverlap(originalAddress, endAddress, key, value, mutable)
      }
      mutable.toMap()
    } ?: eventHashOf()
    return eventMap.replaceEvents(eventType, newEvents)
  }


  private fun Score.adjustPreviousOverlap(
    eventAddress: EventAddress,
    key: EventMapKey,
    value: Event,
    mutable: MutableMap<EventMapKey, Event>
  ) {
    if (value.isTrue(EventParam.END) || key.eventAddress > eventAddress) {
      return
    }
    addDuration(key.eventAddress, value.duration())?.let { oldEnd ->
      if (oldEnd.staveless() >= eventAddress.staveless()) {
        getPreviousStaveSegment(eventAddress.copy(staveId = eventAddress.staveId))
          ?.let { newEnd ->
            val newDuration = getDuration(key.eventAddress, newEnd) ?: value.duration()
            mutable[key] = value.addParam(EventParam.DURATION, newDuration)
            mutable.remove(key.copy(eventAddress = oldEnd))
            mutable.put(
              key.copy(eventAddress = newEnd.staveless()),
              value.addParam(
                EventParam.END to true,
                EventParam.DURATION to newDuration
              )
            )
          }
      }
    }
  }

  private fun Score.adjustNextOverlap(
    eventAddress: EventAddress,
    endAddress: EventAddress,
    key: EventMapKey, value: Event,
    mutable: MutableMap<EventMapKey, Event>
  ) {
    if (!value.isTrue(EventParam.END) && key.eventAddress.horizontal in eventAddress.horizontal..endAddress.horizontal) {
      getNextStaveSegment(endAddress.copy(staveId = eventAddress.staveId))
        ?.let { newStart ->
          val newDuration = getDuration(key.eventAddress, newStart) ?: value.duration()
          mutable.remove(key)
          mutable.put(
            key.copy(eventAddress = newStart.staveless()),
            value.addParam(EventParam.DURATION, newDuration)
          )
        }
    }
  }
}

object LineSubAdder : LineSubAdderIf

