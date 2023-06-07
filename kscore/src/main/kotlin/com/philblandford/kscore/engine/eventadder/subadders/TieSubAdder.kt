package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.representation.MAX_VOICE
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.log.ksLogt


object TieSubAdder : BaseSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    ksLogt("OI")
    return score.fold(score.getNotes(eventAddress).toList()) { (address, note) ->
      findEndAndAdd(address, note)
    }
  }

  private fun Score.findEndAndAdd(
    address: EventAddress,
    note: Note
  ): ScoreResult {
    return getEndNote(address, note).then { endAddress ->
      ksLogt("$endAddress")
      addTie(note, address, endAddress)
    }.failureIsNoop(this)
  }

  private fun Score.addTie(
    note: Note,
    address: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    return setNoteParam(EventParam.IS_START_TIE, true, address)
      .then { it.setNoteParam(EventParam.IS_END_TIE, true, endAddress) }
      .then { it.setNoteParam(EventParam.END_TIE, note.duration, endAddress) }
  }

  private fun <T> Score.setNoteParam(
    eventParam: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    return getEvent(EventType.DURATION, eventAddress.idless())?.let { chord(it) }
      .ifNullError { chord ->
        val newChord = chord.transformNote(eventAddress.id - 1) {
          note(it.toEvent().addParam(eventParam, value)) ?: it
        }
        DurationSubAdder.addEvent(
          this,
          voiceMapDestination,
          EventType.DURATION,
          newChord.toEvent().params.plus(EventParam.HOLD to true),
          eventAddress.idless()
        )
      }
  }

  private fun Score.getNotes(eventAddress: EventAddress): Map<EventAddress, Note> {
    return if (eventAddress.id == 0) {
     getAllNotes(eventAddress)
    } else {
      getEvent(EventType.NOTE, eventAddress)?.let { note(it)?.let { mapOf(eventAddress to it) } }
    } ?: mapOf()
  }

  private fun Score.getAllNotes(eventAddress: EventAddress): Map<EventAddress, Note> {
    return (1..MAX_VOICE).fold(mapOf()) { map, voice ->
      val notes = getEvent(EventType.DURATION, eventAddress.copy(voice = voice))?.let {
        chord(it)?.notes?.withIndex()
          ?.associate { eventAddress.copy(id = it.index + 1, voice = voice) to it.value }
      } ?: mapOf()
      map + notes
    }
  }

  private fun Score.getOtherNote(
    startAddress: EventAddress, startNote: Note,
    jumpOp: (EventAddress, Duration) -> AnyResult<EventAddress>
  ): AnyResult<EventAddress> {
    return jumpOp(startAddress, startNote.realDuration)
      .then { endAddress ->
        getEvent(EventType.DURATION, endAddress.idless()).ifNullError("Event not found") { event ->
          chord(event).ifNullError("Event is not chord") { endChord ->
            val notes = endChord.notes
            notes.withIndex().find { it.value.pitch.midiVal == startNote.pitch.midiVal }
              .ifNullError("Note not found in chord") {
                Right(endAddress.copy(id = it.index + 1))
              }
          }
        }
      }
  }


  private fun Score.getEndNote(
    startAddress: EventAddress, startNote: Note
  ): AnyResult<EventAddress> {
    return getOtherNote(startAddress, startNote) { a, e ->
      addDuration(a, e)?.let { Right(it) } ?: asError("Could not add duration $a to $e")
    }
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    if (eventAddress.id != 0) {
      return score.getEvent(EventType.NOTE, eventAddress).ifNullError("Note not found") {
        note(it).ifNullError("Note not parseable") { note ->
          score.getEndNote(eventAddress, note)
            .then { end ->
              score.deleteTie(eventAddress, end)
            }
        }
      }
    } else {
      val voices = if (eventAddress.voice == 0) (1..MAX_VOICE) else listOf(eventAddress.voice)
      return score.fold(voices) { voice ->
        getEvent(EventType.DURATION, eventAddress).ifNullRestore(score) {
          chord(it).ifNullRestore(score) { chord ->
            this.fold(chord.notes.filter { it.isStartTie }.withIndex()) { iv ->
              deleteEvent(
                this,
                destination,
                eventType,
                params,
                eventAddress.copy(voice = voice, id = iv.index + 1)
              )
            }
          }
        }
      }
    }
  }

  private fun Score.deleteTie(
    address: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    return setNoteParam(EventParam.IS_START_TIE, false, address).then { newScore ->
      newScore.setNoteParam(EventParam.IS_END_TIE, false, endAddress)
        .failureIsNoop(newScore) { it.setNoteParam(EventParam.END_TIE, dZero(), endAddress) }
        .failureIsNoop(newScore)
    }
  }

}