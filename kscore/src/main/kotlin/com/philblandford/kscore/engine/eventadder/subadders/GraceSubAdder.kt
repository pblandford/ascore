package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.core.score.VoiceMap
import com.philblandford.kscore.engine.core.score.voiceMap
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.eventadder.subadders.DurationSubAdder.prepare
import com.philblandford.kscore.engine.eventadder.subadders.NoteSubAdder.adjustDotted
import com.philblandford.kscore.engine.eventadder.util.changeSubLevel
import com.philblandford.kscore.engine.eventadder.util.getOrCreateVoiceMap
import com.philblandford.kscore.engine.eventadder.util.strip
import com.philblandford.kscore.engine.types.*

object GraceSubAdder : BaseSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    val voiceMap = score.getOrCreateVoiceMap(eventAddress)
    val newParams = score.prepare(params, eventAddress)
    return voiceMap.addGrace(eventAddress, newParams).then { vm ->
      score.changeSubLevel(vm, eventAddress)
        .then { it.postAdd(eventAddress, vm) }
    }.then {
      val newGraceOffset = if (params.isTrue(EventParam.HOLD)) eventAddress.graceOffset ?: dZero() else {
        eventAddress.graceOffset?.let { it + (params.g<Duration>(EventParam.DURATION) ?: dZero()) }
      }
      var newGraceAddress = eventAddress.copy(graceOffset = newGraceOffset).voiceIdless()
      if (newGraceOffset != null && it.getEvent(EventType.DURATION, newGraceAddress.copy(voice = eventAddress.voice)) == null) {
        newGraceAddress = newGraceAddress.copy(graceOffset = null)
      }
      MarkerSubAdder.setParam(it, destination, eventType, EventParam.MARKER_POSITION, newGraceAddress, eZero()) }
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    val voiceMap = score.getOrCreateVoiceMap(eventAddress)
    return voiceMap.deleteGrace(eventAddress.strip(ScoreLevelType.VOICEMAP)).then { vm ->
      score.changeSubLevel(vm, eventAddress)
    }.then { MarkerSubAdder.deleteEvent(it, destination, eventType, params, eventAddress) }
  }

  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.getVoiceMap(eventAddress)?.let { voiceMap ->
      voiceMap.eventMap.getEvent(
        EventType.DURATION, eZero().copy(
          offset = eventAddress.offset,
          graceOffset = eventAddress.graceOffset
        )
      )?.let { event ->
        voiceMap.addGrace(eventAddress, event.setModValue(param, value).params).then { newMap ->
          score.changeSubLevel(newMap, eventAddress)
        }
      }
    } ?: Left(NotFound("Voice map not found at $eventAddress"))
  }

  private fun Score.prepare(params: ParamMap, eventAddress: EventAddress): ParamMap {
    var event = Event(EventType.DURATION, params)
    event = chord(event)?.transformNotes { it.copy(isSmall = true) }?.toEvent() ?: event
    event = event.prepare(this, eventAddress, null)
    if (params.g<GraceType>(EventParam.GRACE_TYPE) == GraceType.ACCIACCATURA) {
      event = event.addParam(EventParam.IS_SLASH to true)
    }
    return params.plus(event.params)
  }

  private fun VoiceMap.deleteGrace(
    eventAddress: EventAddress
  ): AnyResult<VoiceMap> {
    return eventAddress.graceOffset?.let { graceOffset ->

      val graceNotes = eventMap.getGraceNotesAtOffset(eventAddress)
      var existing = graceNotes.mapNotNull { (k, v) ->
        k.eventAddress.graceOffset?.let { it to v }
      }.toMap()
      val duration = existing[graceOffset]?.duration() ?: dZero()
      val toDelete =
        graceNotes.toList().maxByOrNull { it.first.eventAddress.graceOffset ?: dZero() }?.let { last ->
          val lastOffset = last.first.eventAddress.graceOffset
          if (graceOffset > lastOffset) lastOffset else graceOffset
        } ?: graceOffset
      existing = existing.minus(toDelete)
      var newEvents = existing.map { (o, v) ->
        if (o < graceOffset) {
          EMK(EventType.DURATION, eventAddress.copy(graceOffset = o)) to v
        } else {
          EMK(EventType.DURATION, eventAddress.copy(graceOffset = o - duration)) to v
        }
      }.toMap()

      newEvents = eventMap.getEvents(EventType.DURATION)?.minus(graceNotes.keys)?.plus(newEvents)
        ?: newEvents
      val newVm = replaceSelf(eventMap.replaceEvents(EventType.DURATION, newEvents)) as VoiceMap
      Right(newVm)
    } ?: Left(Error("Address is not grace"))
  }


  private fun VoiceMap.addGrace(eventAddress: EventAddress, params: ParamMap): AnyResult<VoiceMap> {
    val modified = getGraceAddress(eventAddress)
    val newVm = addWholeBarRest()
    return params.setDuration(modified).then { newParams ->
      if (params.g<GraceInputMode>(EventParam.GRACE_MODE) == GraceInputMode.SHIFT) {
        newVm.addShift(modified, newParams.stripParams())
      } else {
        newVm.addAdd(modified, newParams.stripParams())
      }
    }
  }

  private fun ParamMap.setDuration(eventAddress: EventAddress): AnyResult<ParamMap> {
    var duration =
      g<Duration>(EventParam.DURATION) ?: return Left(Error("No duration specified"))
    duration = duration.adjustDotted(this, eventAddress.graceOffset ?: dZero())
    val notes = g<List<Event>>(EventParam.NOTES)?.map { ev ->
      ev.addParam(EventParam.DURATION to duration, EventParam.REAL_DURATION to duration)
    }
    val newParams = notes?.let { this.plus(EventParam.NOTES to it) } ?: this
    return newParams.plus(
      paramMapOf(
        EventParam.DURATION to duration,
        EventParam.REAL_DURATION to duration
      )
    ).ok()
  }

  private fun VoiceMap.addWholeBarRest(): VoiceMap {
    return if (getEvents(EventType.DURATION).isNullOrEmpty()) {
      val newMap = eventMap.putEvent(eZero(), rest(timeSignature.duration))
      replaceSelf(newMap) as VoiceMap
    } else {
      this
    }
  }

  private fun VoiceMap.addShift(eventAddress: EventAddress, params: ParamMap): AnyResult<VoiceMap> {

    val duration = params.g<Duration>(EventParam.DURATION)
    var existing = eventMap.getGraceNotesAtOffset(eventAddress)
    val existingSorted =
      existing.toList().sortedBy { it.first.eventAddress.graceOffset }.dropWhile {
        it.first.eventAddress.graceOffset?.let { it < eventAddress.graceOffset } ?: true
      }
    existing = existingSorted.toMap()

    var newMap = existing.toList()
      .fold(eventMap) { map, (emk, _) -> map.deleteEvent(emk.eventAddress, emk.eventType) }
    existing = existing.map {
      it.key.copy(
        eventAddress =
        it.key.eventAddress.copy(graceOffset = it.key.eventAddress.graceOffset?.add(duration))
      ) to
          it.value
    }.toMap()
    newMap =
      existing.toList().fold(newMap) { map, (emk, ev) -> map.putEvent(emk.eventAddress, ev) }
    newMap =
      newMap.putEvent(
        eventAddress.staveless().voiceless(), Event(
          EventType.DURATION,
          params.plus(EventParam.DURATION to duration).plus(EventParam.REAL_DURATION to duration)
        )
      )
    return Right(voiceMap(timeSignature, newMap))
  }

  private fun VoiceMap.addAdd(eventAddress: EventAddress, params: ParamMap): AnyResult<VoiceMap> {

    val events = eventMap.putEvent(eventAddress, Event(EventType.DURATION, params))
    return Right(voiceMap(timeSignature, events))
  }

  private fun EventMap.getGraceNotesAtOffset(eventAddress: EventAddress): EventHash {
    return getEvents(EventType.DURATION)?.filter {
      it.key.eventAddress.graceOffset != null &&
          it.key.eventAddress.offset == eventAddress.offset
    } ?: eventHashOf()
  }


  private fun VoiceMap.getGraceAddress(eventAddress: EventAddress): EventAddress {
    return if (!eventAddress.isGrace) {
      val atOffset =
        eventMap.getEvents(EventType.DURATION)?.toList()?.groupBy { it.first.eventAddress.offset }
          ?.get(eventAddress.offset) ?: listOf()
      val existingGrace = atOffset.filter { it.first.eventAddress.isGrace }
        .sortedBy { it.first.eventAddress.graceOffset }
      val last = existingGrace.lastOrNull()
      val end = last?.first?.eventAddress?.graceOffset?.add(last.second.realDuration()) ?: dZero()
      eventAddress.copy(graceOffset = end)
    } else {
      eventAddress
    }.strip(ScoreLevelType.VOICEMAP)
  }

  private fun ParamMap.stripParams(): ParamMap {
    return minus(
      setOf(
        EventParam.GRACE_MODE)
    ).plus(
      EventParam.IS_SLASH to (g<GraceType>(EventParam.GRACE_TYPE) == GraceType.ACCIACCATURA ||
          isTrue(EventParam.IS_SLASH))
    )
  }

}
