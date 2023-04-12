package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.newadder.util.rangeToEventEnd
import com.philblandford.kscore.engine.pitch.getPitchFromMidiVal
import com.philblandford.kscore.engine.types.*

internal object NoteSubAdder : NewSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return score.getVoice(eventAddress, params).then { voiceAddress ->
      doAddEvent(score, destination, params, voiceAddress)
    }
  }


  private fun doAddEvent(
    score: Score,
    destination: EventDestination,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return params.g<Duration>(EventParam.DURATION)?.let { duration ->
      val adjustedDuration =
        if ((params.g<GraceType>(EventParam.GRACE_TYPE) ?: GraceType.NONE) == GraceType.NONE) {
          duration.adjustDotted(params, eventAddress.offset)
        } else duration
      score.getNoteParts(adjustedDuration, eventAddress).then { list ->
        score.addNoteParts(destination, params, list)
      }
    } ?: Left(Error("Duration not specified"))
  }

  private fun Score.addNoteParts(
    destination: EventDestination,
    params: ParamMap,
    list: List<NotePart>
  ): ScoreResult {
    return fold(list) { notePart ->
      addNotePart(
        destination,
        params.plus(EventParam.DURATION to notePart.duration).plus(notePart.extraParams),
        notePart.eventAddress
      )
    }
  }

  private fun Score.addNotePart(
    destination: EventDestination,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return getPassDownEvent(params, eventAddress).then { (chord, idxs) ->
      DurationSubAdder.addEvent(
        this,
        destination,
        EventType.DURATION,
        chord.params,
        eventAddress
      ).then { res ->
        res.fold(idxs) {
          val midiVal = chord.getParam<List<Event>>(EventParam.NOTES)?.getOrNull(it)
            ?.getParam<Pitch>(EventParam.PITCH)?.midiVal ?: 0
          handleTieToLast(eventAddress, params, it, midiVal)
        }
      }
    }
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.removePreviousNoteTie(eventAddress).then {
      it.getDepletedChord(eventAddress).then { chord ->
        if (chord.getParam<List<Event>>(EventParam.NOTES)?.isEmpty() != false) {
          DurationSubAdder.deleteEvent(score, destination, eventType, params, eventAddress)
        } else {
          DurationSubAdder.addEvent(
            score,
            destination,
            EventType.DURATION,
            chord.params,
            eventAddress
          )
        }
      }
    }
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

  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    if (param == EventParam.IS_START_TIE && value == true) {
      return TieSubAdder.addEvent(score, destination, EventType.TIE, paramMapOf(), eventAddress)
    }

    return score.getEvent(EventType.DURATION, eventAddress.copy(id = 0))?.let { chord(it) }
      .ifNullError("Chord not found at $eventAddress") { chord ->
        chord.transformNoteOrFail(eventAddress.id - 1) {
          it.setParam(param, value)
        }.then { newChord ->
          DurationSubAdder.addEvent(
            score,
            destination,
            EventType.DURATION,
            newChord.toEvent().params.plus(EventParam.HOLD to true),
            eventAddress.copy(id = 0)
          )
        }
      }
  }

  private fun <T> Note.setParam(eventParam: EventParam, value: T): AnyResult<Note> {
    return when (eventParam) {
      EventParam.ACCIDENTAL -> setAccidental(value as Accidental).ok()
      else -> note(
        toEvent().addParam(
          eventParam,
          value
        )
      ).ifNullFail("Could not set $eventParam to $value")
    }
  }

  private fun Note.setAccidental(accidental: Accidental): Note {
    return copy(pitch = pitch.copy(accidental = accidental))
  }

  private fun Score.getPassDownEvent(
    params: ParamMap,
    eventAddress: EventAddress
  ): AnyResult<Pair<Event, List<Int>>> {
    return createNote(params, eventAddress).then { note ->
      Right(getChordEvent(note, params, eventAddress))
    }
  }

  private fun Score.getChordEvent(
    note: Note,
    params: ParamMap,
    eventAddress: EventAddress
  ): Pair<Event, List<Int>> {
    val percussion = getInstrument(eventAddress)?.percussion ?: false
    val chord = getChord(eventAddress, params, note.duration, percussion)
    val newChord =
      chord.removeNote { !it.percussion && it.pitch.midiVal == note.pitch.midiVal }.addNote(note)
        .addOctave(note, params)

    val idx = newChord.notes.indexOf(note)
    val octaveIdx =
      if (params[EventParam.ADD_OCTAVE] != null) newChord.notes.indexOf(note.shiftOctave(-1)) else null

    return newChord.toEvent()
      .addParams(
        params.getValues(
          listOf(
            EventParam.GRACE_TYPE,
            EventParam.GRACE_MODE,
            EventParam.HOLD,
            EventParam.DOTTED_RHYTHM
          )
        )
      ) to listOfNotNull(idx, octaveIdx)
  }

  private fun Chord.addOctave(note: Note, params: ParamMap): Chord {
    return if (params.isTrue(EventParam.ADD_OCTAVE)) {
      removeNote { !it.percussion && it.pitch.midiVal == note.pitch.midiVal - 12 }.addNote(
        note.shiftOctave(-1)
      )
    } else this
  }


  private fun Score.getChord(
    eventAddress: EventAddress,
    params: ParamMap,
    duration: Duration,
    percussion:Boolean
  ): Chord {
    return if (params.g<GraceInputMode>(EventParam.GRACE_MODE) == GraceInputMode.SHIFT) {
      Chord(duration, listOf())
    } else {
      getEvent(EventType.DURATION, eventAddress)?.let {
        if (it.duration() == duration)
          chord(it)
        else if (percussion){
          chord(it)?.setDuration(duration)
        } else null
      } ?: Chord(duration, listOf())
    }
  }


  private fun Score.createNote(
    params: ParamMap,
    eventAddress: EventAddress
  ): AnyResult<Note> {
    val octaveShift = getParamAt<Int>(EventType.OCTAVE, EventParam.NUMBER, eventAddress) ?: 0
    return getInstrument(eventAddress)?.let { instrument ->
      val newParams = if (instrument.percussion) {
        getParamsUnpitched(params, instrument)
      } else {
        getParamsPitched(params, octaveShift)
      }
      newParams.then { np ->
        note(Event(EventType.NOTE, np))?.let { Right(it) } ?: Left(Error("Failed creating note"))
      }
    } ?: Left(Error("Could not create note from parameters"))
  }

  private fun getParamsPitched(params: ParamMap, octaveShift: Int): AnyResult<ParamMap> {
    return params.g<Int>(EventParam.MIDIVAL)?.let { midiVal ->
      val accidental = params.g<Accidental>(EventParam.ACCIDENTAL) ?: Accidental.SHARP
      val pitch = getPitchFromMidiVal(midiVal, accidental, octaveShift)
      Right(params.plus(EventParam.PITCH to pitch).plus(EventParam.MIDIVAL to 0))
    } ?: Left(Error("Midi value not specified"))
  }

  private fun getParamsUnpitched(
    paramMap: ParamMap, instrument: Instrument
  ): AnyResult<ParamMap> {
    return paramMap.g<Int>(EventParam.MIDIVAL)?.let { midiVal ->

      instrument.percussionDescrs.find { it.midiId == midiVal }?.let { descr ->
        val params = paramMap.plus(
          paramMapOf(
            EventParam.POSITION to Coord(0, descr.staveLine),
            EventParam.NOTE_HEAD_TYPE to descr.noteHead,
            EventParam.PITCH to unPitched(),
            EventParam.PERCUSSION to true
          )
        )
        Right(params)
      }
    } ?: Left(Error("Could not find midi value in instrument"))
  }

  fun Duration.adjustDotted(params: ParamMap, offset: Offset): Duration {
    return if (params.isTrue(EventParam.DOTTED_RHYTHM)) {
      when {
        offset.divide(this).denominator == 1 -> this.multiply(Duration(3, 2))
        offset.divide(this).denominator != 1 -> this.divide(2)
        else -> this
      }
    } else {
      this
    }
  }

  private fun Score.handleTieToLast(
    eventAddress: EventAddress,
    params: ParamMap,
    noteIdx: Int,
    midiVal: Int
  ): ScoreResult {
    return if (params.isTrue(EventParam.TIE_TO_LAST)) {
      val destination = EventDestination(listOf(ScoreLevelType.VOICEMAP))
      getPreviousChord(eventAddress).ifNullRestore(this) { (previousAddress, previousChord) ->
        if (!previousChord.notes.any { it.pitch.midiVal == midiVal }) {
          Right(this)
        } else {
          setThisNote(
            eventAddress,
            previousChord,
            noteIdx,
            destination
          ).then { newScore ->
            newScore.setPreviousNoteTie(
              previousAddress,
              previousChord,
              midiVal,
              true,
              destination
            )
          }
        }
      }
    } else Right(this)
  }

  private fun Score.getPreviousChord(eventAddress: EventAddress): Pair<EventAddress, Chord>? {
    return getPreviousVoiceSegment(eventAddress)?.let { previous ->
      getEvent(EventType.DURATION, previous)?.let { event ->
        chord(event)?.let { previous to it }
      }
    }
  }

  private fun Score.setThisNote(
    eventAddress: EventAddress,
    previousChord: Chord,
    noteIdx: Int, destination: EventDestination
  ): ScoreResult {

    val noteAddress = eventAddress.copy(id = noteIdx + 1)
    return getEvent(EventType.NOTE, noteAddress)?.let { noteEvent ->
      noteEvent.addParam(
        EventParam.IS_END_TIE to true,
        EventParam.END_TIE to previousChord.duration
      )
      setParam(
        this,
        destination,
        EventType.NOTE,
        EventParam.IS_END_TIE,
        true,
        noteAddress
      ).then {
        setParam(
          it,
          destination,
          EventType.NOTE,
          EventParam.END_TIE,
          previousChord.duration,
          noteAddress
        )
      }

    } ?: Left(Error("Could not find note just added at $eventAddress"))
  }


  private fun Score.removePreviousNoteTie(eventAddress: EventAddress): ScoreResult {
    val destination = EventDestination(listOf(ScoreLevelType.VOICEMAP))
    return getEvent(EventType.NOTE, eventAddress)?.let { note ->
      getPreviousChord(eventAddress)?.let { (previousAddress, previousChord) ->
        setPreviousNoteTie(
          previousAddress,
          previousChord,
          note.getParam<Pitch>(EventParam.PITCH)?.midiVal ?: 0,
          false,
          destination
        )
      }
    } ?: Right(this)
  }

  private fun Score.setPreviousNoteTie(
    previousAddress: EventAddress,
    previousChord: Chord,
    thisNoteMidi: Int,
    tie: Boolean,
    destination: EventDestination
  ): ScoreResult {
    return previousChord.notes.withIndex().find { it.value.pitch.midiVal == thisNoteMidi }
      .ifNullRestore(this) { iv ->
        setParam(
          this, destination, EventType.NOTE, EventParam.IS_START_TIE, tie,
          previousAddress.copy(id = iv.index + 1)
        )
      }
  }


  private fun Score.getDepletedChord(eventAddress: EventAddress): EventResult {
    return getEvent(EventType.DURATION, eventAddress)?.let { chord(it) }?.let { chord ->
      Right(chord.removeNote(eventAddress.id - 1).toEvent())
    } ?: Left(NotFound("Chord not found at $eventAddress"))
  }

  private data class NotePart(
    val eventAddress: EventAddress, val duration: Duration,
    val extraParams: ParamMap = paramMapOf()
  )

  private fun Score.getNoteParts(
    duration: Duration,
    eventAddress: EventAddress
  ): AnyResult<List<NotePart>> {
    val noteAddressesDurations = divideDuration(duration, eventAddress)
    var lastDuration = dZero()
    return noteAddressesDurations.then { list ->
      val parts = list.withIndex().map { iv ->
        var params = paramMapOf()
        if (iv.index < list.size - 1 && eventAddress.barNum != numBars) {
          params = params.plus(EventParam.IS_START_TIE to true)
        }
        if (iv.index > 0) {
          params =
            params.plus(EventParam.IS_END_TIE to true).plus(EventParam.END_TIE to lastDuration)
        }
        lastDuration = iv.value.second
        NotePart(iv.value.first, iv.value.second, params)
      }
      Right(parts)
    }
  }

  private fun Score.getVoice(
    eventAddress: EventAddress,
    params: ParamMap
  ): AnyResult<EventAddress> {
    return params[EventParam.MIDIVAL].ifNullError("Midi val not found") { midiVal ->
      getInstrument(eventAddress).ifNullError("Instrument not found at $eventAddress") { instrument ->
        if (instrument.percussion && instrument.staveLines > 1) {
          instrument.percussionDescrs.find { it.midiId == midiVal }
            .ifNullError("Could not find sound for midi id $midiVal") { descr ->
              val voice = if (descr.up) 1 else 2
              eventAddress.copy(voice = voice).ok()
            }
        } else {
          eventAddress.ok()
        }
      }
    }
  }

}