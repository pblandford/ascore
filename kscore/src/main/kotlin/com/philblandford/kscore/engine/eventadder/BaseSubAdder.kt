package com.philblandford.kscore.engine.eventadder

import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.eventadder.util.changeSubLevel
import com.philblandford.kscore.engine.eventadder.util.getLevel
import com.philblandford.kscore.engine.eventadder.util.strip
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLogt

typealias ScoreLevelResult = AnyResult<ScoreLevel>
typealias ScoreResult = AnyResult<Score>
typealias PartResult = AnyResult<Part>
typealias StaveResult = AnyResult<Stave>
typealias BarResult = AnyResult<Bar>
typealias VoiceMapResult = AnyResult<VoiceMap>
typealias EventResult = AnyResult<Event>

data class EventDestination(
  var levels: List<ScoreLevelType>,
  val eventAdder: BaseSubAdder? = null
)

val voiceMapDestination = EventDestination(listOf(ScoreLevelType.VOICEMAP))
val barDestination = EventDestination(listOf(ScoreLevelType.BAR))
val staveDestination = EventDestination(listOf(ScoreLevelType.STAVE))
val partDestination = EventDestination(listOf(ScoreLevelType.PART))
val scoreDestination = EventDestination(listOf(ScoreLevelType.SCORE))

typealias AddListener = (Score, EventDestination, EventType, ParamMap, EventAddress) -> ScoreResult
typealias DeleteListener = (Score, EventDestination, EventType, ParamMap, EventAddress) -> ScoreResult
typealias RangeListener = (Score, EventDestination, EventType, ParamMap, EventAddress, EventAddress) -> ScoreResult

interface BaseSubAdder {

  val addListeners: Map<EventType, AddListener>
    get() = mapOf()
  val deleteListeners: Map<EventType, DeleteListener>
    get() = mapOf()
  val rangeListeners: Map<EventType, RangeListener>
    get() = mapOf()

  fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    ksLogt("$eventType $params $eventAddress")
    return score.getLevel(destination, eventAddress, eventType)?.let { level ->
      val newMap = level.eventMap.putEvent(
        eventAddress.strip(level.scoreLevelType, eventType),
        Event(eventType, params)
      )
      val newLevel = level.replaceSelf(newMap)
      score.changeSubLevel(newLevel, eventAddress)
    } ?: Left(Error("Could not find sublevel for $eventType $destination $eventAddress"))
  }

  fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.getLevel(destination, eventAddress)?.let { level ->
      val newMap = level.eventMap.deleteEvent(
        eventAddress.strip(level.scoreLevelType, eventType),
        eventType
      )
      val newLevel = level.replaceSelf(newMap)
      score.changeSubLevel(newLevel, eventAddress)
    } ?: Left(Error("Could not find sublevel"))
  }

  fun addEventRange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    val events = score.getEvents(EventType.DURATION, eventAddress, endAddress)?.toList() ?: listOf()
    return events.toList().fold(Right(score) as ScoreResult) { sr, (k, v) ->
      sr.then { addEvent(it, destination, eventType, params, k.eventAddress) }
    }
  }

  fun deleteEventRange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    val events = score.getEvents(eventType, eventAddress, endAddress)?.toList()
    return events?.fold(Right(score) as ScoreResult) { sr, (key, _) ->
      sr.then { score ->
        when (val res = deleteEvent(
          score,
          destination,
          eventType,
          paramMapOf(EventParam.CONSOLIDATE to true),
          key.eventAddress
        )) {
          is Left -> when (res.l) {
            is HarmlessFailure -> Right(score)
            else -> res
          }

          else -> res
        }
      }.then { it.resetMarker(score) }
    } ?: Left(HarmlessFailure("Could not get events of type $eventType"))
  }

  fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    val event = score.getEvent(eventType, eventAddress) ?: Event(eventType)
    return addEvent(
      score,
      destination,
      event.eventType,
      event.params.plus(param to value),
      eventAddress
    )
  }

  fun <T> setParamRange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    start: EventAddress,
    end: EventAddress
  ): ScoreResult {
    val events = score.getEvents(eventType, start, end)?.toList() ?: listOf()
    ksLogt("$start $end $events")
    return events.fold(Right(score) as ScoreResult) { sr, (emk, event) ->
      sr.then {
        ksLogt("$emk $event")
        setParam(
          it,
          destination,
          event.eventType,
          param,
          value,
          emk.eventAddress
        )
      }
    }
  }

  private fun Score.resetMarker(originalScore: Score): ScoreResult {
    return originalScore.getParam<EventAddress>(
      EventType.UISTATE,
      EventParam.MARKER_POSITION,
      eZero()
    )?.let { marker ->
      getFloorStaveSegment(marker)?.let { floor ->
        NewEventAdder.setParam(
          this,
          EventType.UISTATE,
          EventParam.MARKER_POSITION,
          floor,
          eZero()
        )
      }
    } ?: Right(this)
  }

}

object GenericSubAdder : BaseSubAdder

interface NewEventAdderIf {
  fun getDestination(eventType: EventType): EventDestination? = destinations[eventType]

  fun addEvent(
    score: Score,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    ksLogt("$eventType $params")
    return getDestination(eventType)?.let { destination ->
      val adder = destination.eventAdder ?: GenericSubAdder
      adder.addEvent(score, destination, eventType, params, eventAddress.prepare(destination))
        .then {
          handleAddListeners(it, destination, eventType, params, eventAddress)
        }
    } ?: Left(NoDestinationFailure(eventType))
  }

  fun deleteEvent(
    score: Score,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return getDestination(eventType)?.let { destination ->
      val adder = destination.eventAdder ?: GenericSubAdder
      adder.deleteEvent(score, destination, eventType, params, eventAddress).then {
        handleDeleteListeners(it, destination, eventType, params, eventAddress)
      }
    } ?: Left(NoDestinationFailure(eventType))
  }

  fun addEventRange(
    score: Score,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    return getDestination(eventType)?.let { destination ->
      val adder = destination.eventAdder ?: GenericSubAdder
      adder.addEventRange(score, destination, eventType, params, eventAddress, endAddress).then {
        handleRangeListeners(it, destination, eventType, params, eventAddress, endAddress)
      }
    } ?: Left(NoDestinationFailure(eventType))
  }

  fun deleteEventRange(
    score: Score,
    eventType: EventType,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    return getDestination(eventType)?.let { destination ->
      val adder = destination.eventAdder ?: GenericSubAdder
      adder.deleteEventRange(score, destination, eventType, eventAddress, endAddress).then {
        handleRangeListeners(it, partDestination, eventType, paramMapOf(), eventAddress, endAddress)
      }
    } ?: Left(NoDestinationFailure(eventType))
  }

  fun deleteRange(
    score: Score, eventAddress: EventAddress, endAddress: EventAddress
  ): ScoreResult {
    val deletables = listOf(
      EventType.DURATION, EventType.CLEF, EventType.TUPLET, EventType.SLUR,
      EventType.HARMONY, EventType.LYRIC
    )
    val segmentEnd = score.getLastSegmentInDuration(endAddress)?.let { lastSegment ->
      endAddress.copy(offset = lastSegment.offset)
    } ?: endAddress
    return deletables.fold(Right(score) as ScoreResult) { sr, et ->
      sr.then { deleteEventRange(it, et, eventAddress, segmentEnd) }.then {
        handleRangeListeners(it, partDestination, EventType.DURATION, paramMapOf(), eventAddress, endAddress)
      }
    }
  }

  fun <T> setParam(
    score: Score,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    return getDestination(eventType)?.let { destination ->
      val adder = destination.eventAdder ?: GenericSubAdder
      adder.setParam(score, destination, eventType, param, value, eventAddress).then {
        it.getEvent(eventType, eventAddress)?.let { event ->
          handleAddListeners(it, destination, eventType, event.params, eventAddress)
        } ?: it.ok()
      }
    } ?: Left(NoDestinationFailure(eventType))
  }

  fun <T> setParamRange(
    score: Score,
    eventType: EventType,
    param: EventParam,
    value: T,
    start: EventAddress,
    end: EventAddress
  ): ScoreResult {
    return getDestination(eventType)?.let { destination ->
      val adder = destination.eventAdder ?: GenericSubAdder
      adder.setParamRange(score, destination, eventType, param, value, start, end).then {
        handleRangeListeners(it, destination, eventType, paramMapOf(), start, end)
      }
    } ?: Left(NoDestinationFailure(eventType))
  }

  private fun EventAddress.prepare(destination: EventDestination): EventAddress {
    return if (destination.levels.size == 1) {
      when (destination.levels.first()) {
        ScoreLevelType.VOICEMAP -> if (voice == 0) copy(voice = 1) else this
        else -> this
      }
    } else this
  }

  private fun handleAddListeners(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.fold(allDestinations.toList()) { dest ->
      dest.eventAdder?.addListeners?.get(eventType)?.let { func ->
        func(this, destination, eventType, params, eventAddress)
      } ?: this.ok()
    }
  }

  private fun handleDeleteListeners(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.fold(allDestinations.toList()) { dest ->
      dest.eventAdder?.deleteListeners?.get(eventType)?.let { func ->
        func(this, destination, eventType, params, eventAddress)
      } ?: this.ok()
    }
  }

  private fun handleRangeListeners(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    start: EventAddress,
    end: EventAddress
  ): ScoreResult {
    return score.fold(allDestinations.toList()) { dest ->
      dest.eventAdder?.rangeListeners?.get(eventType)?.let { func ->
        func(this, destination, eventType, params, start, end)
      } ?: this.ok()
    }
  }
}

object NewEventAdder : NewEventAdderIf
