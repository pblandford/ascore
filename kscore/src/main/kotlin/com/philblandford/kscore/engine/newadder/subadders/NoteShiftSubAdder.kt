package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.types.*

internal object NoteShiftSubAdder : NewSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return params.g<Int>(EventParam.AMOUNT)?.let { amount ->
      val accidental = params.g<Accidental>(EventParam.ACCIDENTAL)
      score.handleChord(amount, accidental, params, eventAddress)
    } ?: Left(ParamsMissing(listOf(EventParam.AMOUNT, EventParam.ACCIDENTAL)))
  }


  override fun addEventRange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    val events = score.getEvents(EventType.DURATION, eventAddress, endAddress)?.toList()
      ?.sortedBy { it.first.eventAddress } ?: listOf()
    return score.fold(events) { (key, _) ->
      val extraParams =
        if (key.eventAddress.horizontal == events.first().first.eventAddress.horizontal) {
          paramMapOf(EventParam.IS_START_TIE to true)
        } else if (key.eventAddress.horizontal == events.last().first.eventAddress.horizontal) {
          paramMapOf(EventParam.IS_END_TIE to true)
        } else {
          paramMapOf(EventParam.IS_START_TIE to true, EventParam.IS_END_TIE to true)
        }
      addEvent(this, destination, eventType, params.plus(extraParams), key.eventAddress)
    }
  }

  private fun Score.handleChord(
    amount: Int,
    accidental: Accidental?,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return getEvent(EventType.DURATION, eventAddress.idless())?.let { chord(it) }?.let { chord ->
      val victims = chord.notes.withIndex()
        .filter { iv -> eventAddress.id == 0 || eventAddress.id == iv.index + 1 }
      val shifted = chord.notes.withIndex().map { iv ->
        if (eventAddress.id == 0 || eventAddress.id == iv.index + 1) {
          val acc = accidental ?: iv.value.pitch.accidental
          iv.value.shift(amount, acc)
        } else {
          iv.value
        }
      }
      val newChord = chord.copy(notes = shifted).toEvent()
      DurationSubAdder.addEvent(
        this, EventDestination(listOf(ScoreLevelType.VOICEMAP)),
        EventType.DURATION, newChord.params, eventAddress.idless()
      )
        .then {
          it.fold(victims) { note ->
            it.handleTie(params, note.value, eventAddress)
          }
        }
    } ?: Right(this)
  }

  private fun Note.shift(amount: Int, accidental: Accidental): Note {
    val pitch = getNoteShift(pitch, amount, accidental)
    return copy(pitch = pitch)
  }

  private fun Score.handleTie(
    params: ParamMap,
    note: Note, eventAddress: EventAddress
  ): ScoreResult {
    return handleTiePrevious(params, note, eventAddress)
      .then { it.handleTieNext(params, note, eventAddress) }
  }

  private fun Score.handleTiePrevious(
    params: ParamMap,
    note: Note,
    eventAddress: EventAddress
  ): ScoreResult {
    if (!params.isTrue(EventParam.IS_END_TIE) && note.isEndTie && note.endTie > dZero()) {
      subtractDuration(eventAddress, note.endTie)?.let { previous ->
        getSameNote(previous, note)?.let { (_, previousAddress) ->
          return addEvent(
            this, voiceMapDestination, EventType.NOTE_SHIFT,
            params.plus(EventParam.IS_START_TIE to true), previousAddress
          )
        }
      }
    }
    return Right(this)
  }

  private fun Score.handleTieNext(
    params: ParamMap,
    note: Note,
    eventAddress: EventAddress
  ): ScoreResult {
    if (!params.isTrue(EventParam.IS_START_TIE) && note.isStartTie) {
      addDuration(eventAddress, note.realDuration)?.let { next ->
        getSameNote(next, note)?.let { (_, nextAddress) ->
          return NoteShiftSubAdder.addEvent(
            this, voiceMapDestination, EventType.NOTE_SHIFT,
            params.plus(EventParam.IS_END_TIE to true), nextAddress
          )
        }
      }
    }
    return Right(this)
  }

  private fun Score.getSameNote(eventAddress: EventAddress, note: Note): Pair<Note, EventAddress>? {
    return getEvent(EventType.DURATION, eventAddress)?.let { chord(it) }?.let { chord ->
      chord.notes.withIndex().find { it.value.pitch.midiVal == note.pitch.midiVal }
        ?.let { it.value to eventAddress.copy(id = it.index + 1) }
    }
  }
}
