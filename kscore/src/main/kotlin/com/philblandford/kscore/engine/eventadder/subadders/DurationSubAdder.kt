package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Bar
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.core.score.VoiceMap
import com.philblandford.kscore.engine.dsl.empty
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.eventadder.duration.DEvent
import com.philblandford.kscore.engine.eventadder.duration.add
import com.philblandford.kscore.engine.eventadder.duration.delete
import com.philblandford.kscore.engine.eventadder.util.*
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.util.isPower2

object DurationSubAdder : BaseEventAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return if (isGrace(eventAddress, params)) {
      GraceSubAdder.addEvent(score, destination, eventType, params, eventAddress)
    } else {
      score.addEventNormal(destination, eventType, params, eventAddress)
    }
  }

  private fun Score.addEventNormal(
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return getEventParts(Event(EventType.DURATION, params), eventAddress).then {
      fold(it) { (ea, ev) ->
        doAdd(destination, eventType, ev.params, ea)
      }
    }
  }

  private fun Score.doAdd(
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {


    return deleteDifferentDuration(params, eventAddress).then { s ->

      val vm = s.getOrCreateVoiceMap(eventAddress)

      val voiceOpt = s.getBar(eventAddress)?.voiceNumberMap?.get(eventAddress.offset)
        ?.let { if (it > 1) eventAddress.voice else null }

      val event =
        Event(eventType, params.minus(EventParam.HOLD)).prepare(this, eventAddress, voiceOpt)

      vm.handleTuplet(event, eventAddress).otherwise {
        vm.addDurationEvent(event, eventAddress.offset)
      }
        .then {
          s.postAdd(eventAddress, it)
        }
        .then {
          MarkerSubAdder.addEvent(it, destination, eventType, params, eventAddress)
        }
    }
  }

  private fun Score.deleteDifferentDuration(
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return getParam<Duration>(EventType.DURATION, EventParam.DURATION, eventAddress).ifNullRestore(
      this
    ) { duration ->
      if (duration != params.g<Duration>(EventParam.DURATION)) {
        deleteEvent(
          this,
          voiceMapDestination,
          EventType.DURATION,
          params.plus(EventParam.HOLD to true),
          eventAddress
        )
      } else this.ok()
    }
  }

  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    if (eventAddress.isGrace) {
      return GraceSubAdder.setParam(score, destination, eventType, param, value, eventAddress)
    }
    return score.getVoiceMap(eventAddress).ifNullError { voiceMap ->
      voiceMap.getEvent(EventType.DURATION, ez(0, eventAddress.offset)).ifNullError { ev ->
        val params = ev.setValue(param, value).params.plus(EventParam.HOLD to true)
        addEvent(score, destination, eventType, params, eventAddress)
      }.otherwise {
        if (param == EventParam.TYPE) {
          addEvent(
            score, destination, EventType.DURATION, paramMapOf(
              EventParam.TYPE to value,
              EventParam.DURATION to voiceMap.timeSignature.duration,
              EventParam.HOLD to true
            ), eventAddress
          )
        } else {
          asError("Could not get event at $eventAddress")
        }
      }
    }
  }


  private fun <T> Event.setValue(param: EventParam, value: T): Event {
    return if (param == EventParam.DURATION) {
      setDuration(value as Duration)
    } else if (param == EventParam.TYPE) {
      addParam(param, value)
    } else if (param == EventParam.IS_UPSTEM && value == null) {
      removeParam(param)
    } else {
      setModValue(param, value, true)
    }
  }

  private fun Event.setDuration(value: Duration): Event {
    return chord(this)?.setDuration(value)?.toEvent() ?: run {
      addParam(EventParam.DURATION, value)
    }
  }

  private fun Score.getEventParts(
    event: Event,
    eventAddress: EventAddress
  ): AnyResult<List<Pair<EventAddress, Event>>> {
    val addressesDurations = divideDuration(event.duration(), eventAddress)
    var lastDuration = dZero()
    return addressesDurations.then { list ->
      list.withIndex().mapOrFail { iv ->
        val splitEvent =
          event.split(
            iv.index,
            list.size,
            iv.value.second,
            lastDuration,
            eventAddress.barNum == numBars
          )
        lastDuration = iv.value.second
        splitEvent.verify(iv.value.first)
      }
    }
  }

  private fun Event.verify(eventAddress: EventAddress): AnyResult<Pair<EventAddress, Event>> {
    return if (duration() <= dZero()) {
      Left(Error("Split duration resulted in ${duration()} at $eventAddress"))
    } else {
      Right(eventAddress to this)
    }
  }

  private fun Event.split(
    index: Int, size: Int, thisDuration: Duration,
    lastDuration: Duration, lastBar: Boolean
  ): Event {
    return chord(this)?.let { chord ->
      var newChord = chord
      if (index < size - 1 && !lastBar) {
        newChord = newChord.transformNotes { it.copy(isStartTie = true) }
      }
      if (index > 0) {
        newChord = newChord.transformNotes { it.copy(isEndTie = true, endTie = lastDuration) }
      }
      newChord = newChord.setDuration(thisDuration)
      Event(eventType, params.plus(newChord.toEvent().params))
    } ?: addParam(EventParam.DURATION to thisDuration, EventParam.REAL_DURATION to thisDuration)
  }

  override fun addEventRange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    return params.g<Duration>(EventParam.DURATION)?.let { duration ->
      score.rangeToEventEnd(duration, eventAddress, endAddress) {
        addEvent(this, destination, eventType, params, it)
      }
    } ?: Left(ParamsMissing(listOf(EventParam.DURATION)))
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    if (isGrace(eventAddress, paramMapOf())) {
      return GraceSubAdder.deleteEvent(score, destination, eventType, params, eventAddress)
    }

    return score.removePreviousNoteTies(eventAddress)
      .then { it.removeNextNoteTies(eventAddress) }
      .then { it.doDelete(eventAddress, params) }
      .then { MarkerSubAdder.addEvent(it, destination, eventType, params, eventAddress) }
  }

  private fun Score.doDelete(
    eventAddress: EventAddress, params: ParamMap
  ): ScoreResult {
    return getVoiceMap(eventAddress)?.let { vm ->
      vm.handleTupletDelete(eventAddress, params)
        .otherwise { vm.deleteDurationEvent(eventAddress, params) }
        .then { postAdd(eventAddress, it) }
    } ?: Warning("Voicemap not found at $eventAddress", this)
  }

  fun Event.prepare(score: Score, eventAddress: EventAddress, voice: Int?): Event {
    val octaveShift = score.getOctaveShift(eventAddress)
    return if (subType == DurationType.CHORD) {
      (score.getEventAt(EventType.CLEF, eventAddress)?.second?.subType as ClefType?)?.let { clef ->
        setAllPositions(this, clef, voice, -octaveShift, false)
      } ?: this
    } else {
      this
    }
  }


  private fun VoiceMap.handleTuplet(event: Event, eventAddress: EventAddress): VoiceMapResult {
    return getTuplet(eventAddress.offset)?.let { tuplet ->
      tuplet.addDurationEvent(event, tuplet.stripAddress(eventAddress, EventType.DURATION).offset)
        .then {
          Right(replaceSubLevel(it, subLevels.indexOf(tuplet)) as VoiceMap)
        }
    } ?: if (!eventAddress.offset.denominator.isPower2()) {
      Warning("Not adding note to tuplet in different voice", this)
    } else {
      AbortNoError("No tuplet")
    }
  }

  private fun VoiceMap.handleTupletDelete(
    eventAddress: EventAddress,
    params: ParamMap
  ): VoiceMapResult {
    return getTuplet(eventAddress.offset)?.let { tuplet ->
      tuplet.deleteDurationEvent(tuplet.stripAddress(eventAddress, EventType.DURATION), params)
        .then {
          Right(replaceSubLevel(it, subLevels.indexOf(tuplet)) as VoiceMap)
        }
    } ?: AbortNoError("No tuplet")
  }

  private fun VoiceMap.addDurationEvent(event: Event, offset: Offset): VoiceMapResult {
    val events =
      eventMap.putEvent(ez(0, offset), event.stripParams()).getEvents(EventType.DURATION)
        ?: eventHashOf()
    val durationMap =
      (eventMap.getEvents(EventType.DURATION) ?: eventHashOf()).toDurationMap(timeSignature)
    val consolidate = event.isTrue(EventParam.CONSOLIDATE)

    return durationMap.add(
      DEvent(event.duration(), event.subType as DurationType),
      offset,
      consolidate
    ).then {
      postAddDurationEvent(it, events, offset)
    }
  }

  fun VoiceMap.deleteDurationEvent(eventAddress: EventAddress, params: ParamMap): VoiceMapResult {
    replaceWithEmpty(eventAddress, params)?.let { return Right(it) }
    val offset = eventAddress.offset
    val events =
      eventMap.deleteEvent(ez(0, offset), EventType.DURATION).getEvents(EventType.DURATION)
        ?: eventHashOf()
    val durationMap =
      (eventMap.getEvents(EventType.DURATION) ?: eventHashOf()).toDurationMap(timeSignature)
    val consolidate =
      params.isTrue(EventParam.CONSOLIDATE) || durationMap.map[offset]?.type != DurationType.REST
    return durationMap.delete(offset, consolidate)
      .then {
        postAddDurationEvent(it, events, offset)
      }
  }

  private fun VoiceMap.replaceWithEmpty(eventAddress: EventAddress, params: ParamMap): VoiceMap? {
    if (eventAddress.voice != 1 && !params.isTrue(EventParam.CONSOLIDATE)) {
      eventMap.getEvent(EventType.DURATION, ez(0, eventAddress.offset))?.let { event ->
        if (event.subType == DurationType.REST) {
          var newEm = eventMap.putEvent(ez(0, eventAddress.offset), empty(event.duration()))
          if (newEm.getEvents(EventType.DURATION)
              ?.any { it.value.subType != DurationType.EMPTY } == false
          ) {
            newEm = eventMap.deleteAll(EventType.DURATION)
          }
          return replaceSelf(newEm, subLevels) as VoiceMap
        }
      }
    }
    return null
  }

  private fun Event.stripParams(): Event {
    return copy(params = params.minus(setOf(EventParam.CONSOLIDATE)))
  }

  private fun Score.removePreviousNoteTies(eventAddress: EventAddress): ScoreResult {
    val destination = EventDestination(listOf(ScoreLevelType.VOICEMAP))
    return getEvent(EventType.DURATION, eventAddress)?.let { chord(it) }?.let {
      getPreviousStaveSegment(eventAddress)?.let { previous ->
        getEvent(EventType.DURATION, previous.copy(voice = eventAddress.voice))?.let { chord(it) }?.let { chord ->
          chord.notes.withIndex().filter { it.value.isStartTie }
            .fold(Right(this) as ScoreResult) { s, iv ->
              s.then {
                NoteSubAdder.setParam(
                  it, destination, EventType.NOTE, EventParam.IS_START_TIE, false,
                  previous.copy(voice = eventAddress.voice, id = iv.index + 1)
                )
              }
            }
        }
      }
    } ?: Right(this)
  }

  private fun Score.removeNextNoteTies(eventAddress: EventAddress): ScoreResult {
    val destination = EventDestination(listOf(ScoreLevelType.VOICEMAP))
    return getEvent(EventType.DURATION, eventAddress)?.let { chord(it) }?.let {
      getNextStaveSegment(eventAddress)?.let { next ->
        getEvent(EventType.DURATION, next)?.let { chord(it) }?.let { chord ->
          chord.notes.withIndex().filter { it.value.isEndTie }
            .fold(Right(this) as ScoreResult) { s, iv ->
              s.then {
                NoteSubAdder.setParam(
                  it, destination, EventType.NOTE, EventParam.IS_END_TIE, false,
                  next.copy(voice = eventAddress.voice, id = iv.index + 1)
                )
              }
            }
        }
      }
    } ?: Right(this)
  }


  private fun isGrace(eventAddress: EventAddress, params: ParamMap): Boolean {
    return eventAddress.isGrace ||
        (params.g<GraceType>(EventParam.GRACE_TYPE) ?: GraceType.NONE) != GraceType.NONE
  }

}

internal fun Score.postAdd(eventAddress: EventAddress, newVoiceMap: VoiceMap): ScoreResult {
  return postAddBar(eventAddress, newVoiceMap).then { it.postAddStave(eventAddress) }
}

internal fun Score.postAddBar(eventAddress: EventAddress, newVoiceMap: VoiceMap): ScoreResult {
  return getBar(eventAddress).ifNullRestore(this) { bar ->

    val newBar = if (eventAddress.voice != 1 && newVoiceMap.getVoiceEvents().isEmpty()) {
      bar.removeSubLevels(eventAddress.voice - 1, eventAddress.voice - 1) as Bar
    } else {
      bar.replaceSubLevel(newVoiceMap, eventAddress.voice) as Bar
    }
    newBar.setStems().then {
      it.setAccidentals(this, eventAddress)
    }.then {
      changeSubLevel(it, eventAddress)
    }

  }

}

internal fun Score.postAddStave(eventAddress: EventAddress): ScoreResult {

  return getStave(eventAddress.staveId)?.let { stave ->
    var newStave = stave.deleteEventFromMap(EventType.REPEAT_BAR, eventAddress.staveless())
    newStave.eventMap.getParam<Int>(
      EventType.REPEAT_BAR,
      EventParam.NUMBER,
      eventAddress.dec().staveless()
    )?.let {
      if (it == 2) {
        newStave = newStave.deleteEventFromMap(EventType.REPEAT_BAR, eventAddress.dec().staveless())
      }
    }
    changeSubLevel(newStave, eventAddress)
  } ?: Left(NotFound("Stave not found"))
}

