package com.philblandford.kscore.engine.newadder.util

import com.philblandford.kscore.engine.core.score.VoiceMap
import com.philblandford.kscore.engine.core.score.VoiceNumberMap
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.newadder.Right
import com.philblandford.kscore.engine.newadder.VoiceMapResult
import com.philblandford.kscore.engine.newadder.duration.*
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*


fun VoiceMap.setStems(voiceNumberMap: VoiceNumberMap, voice: Voice): VoiceMap {
  var events = getEvents(EventType.DURATION) ?: eventHashOf()
  events = events.map { (key, event) ->
    val numVoices = voiceNumberMap[key.eventAddress.offset] ?: 1
    key to event.setStem(numVoices, voice)
  }.toMap()
  return replaceVoiceEvents(events)
}

fun VoiceMap.tidy(): VoiceMapResult {
  val events = eventMap.getEvents(EventType.DURATION) ?: eventHashOf()
  var durationMap = events.toDurationMap(timeSignature)
  durationMap = durationMap.reset()
  return postAddDurationEvent(durationMap, events, dZero())
}

fun VoiceMap.postAddDurationEvent(
  durationMap: DurationMap, events: EventHash,
  offset: Offset
): VoiceMapResult {
  val dm = durationMap.tidy()
  val newEvents = events.applyDMap(dm, offset)
  return Right(replaceSelf(eventMap.replaceEvents(EventType.DURATION, newEvents)) as VoiceMap)
}


private fun EventHash.applyDMap(durationMap: DurationMap, newEventOffset: Offset): EventHash {
  var newHash =
    filterNot { it.value.subType == DurationType.REST || durationMap.map[it.key.eventAddress.offset] == null }
  val newEventKey = EMK(
    EventType.DURATION,
    ez(0, newEventOffset)
  )
  newHash[newEventKey]?.let { newEvent ->
    durationMap.map[newEventOffset]?.let { adjustedEvent ->
      newHash = newHash.plus(
        newEventKey to newEvent.addParam(
          EventParam.DURATION to adjustedEvent.duration,
          EventParam.REAL_DURATION to adjustedEvent.duration
        )
      )
    }
  }
  newHash =
    durationMap.map.filter { it.value.type == DurationType.REST }
      .toList()
      .fold(newHash) { eh, (o, r) ->
        eh.plus(
          com.philblandford.kscore.engine.map.EventMapKey(
            EventType.DURATION,
            ez(0, o)
          ) to com.philblandford.kscore.engine.dsl.rest(r.duration)
        )
      }
  val grace =
    filter {
      it.key.eventAddress.isGrace && (
          it.key.eventAddress.offset == dZero() || durationMap.map[it.key.eventAddress.offset] != null)
    }

  return newHash.plus(grace).toMap()
}

fun EventHash.toDurationMap(timeSignature: TimeSignature): DurationMap {
  val events = map { (k, v) -> k.eventAddress.offset to v.toDEvent() }
  return DurationMap(
    timeSignature,
    events.toMap().toSortedMap()
  )
}

fun VoiceMap.getNotes(): Map<EventAddress, Note> {
  return getEvents(EventType.DURATION)?.let { events ->
    events.flatMap { (k, event) ->
      chord(event)?.let { chord ->
        chord.notes.withIndex().map { k.eventAddress.copy(id = it.index + 1) to it.value }
      } ?: listOf()
    }
  }?.toMap() ?: mapOf()
}

private fun Event.toDEvent(): DEvent {
  return DEvent(duration(), subType as DurationType)
}
