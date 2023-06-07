package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.*

internal object NoteShiftSubAdder : BaseSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return params.g<Int>(EventParam.AMOUNT)?.let { amount ->
      val accidental = params.g<Accidental>(EventParam.ACCIDENTAL)
      score.handleChord(amount, accidental, eventAddress, null)
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
    return score.fold(events) { (key, s) ->
      params.g<Int>(EventParam.AMOUNT)?.let { amount ->
        val accidental = params.g<Accidental>(EventParam.ACCIDENTAL)
        handleChord(amount, accidental, key.eventAddress, endAddress)
      } ?: Left(ParamsMissing(listOf(EventParam.AMOUNT, EventParam.ACCIDENTAL)))
    }
  }

  private fun Score.handleChord(
    amount: Int,
    accidental: Accidental?,
    eventAddress: EventAddress,
    endAddress: EventAddress?
  ): ScoreResult {
    val chordAddress = eventAddress.idless()
    return getEvent(EventType.DURATION, chordAddress)?.let { chord(it) }?.let { chord ->
      doHandleChord(amount, accidental, chord, eventAddress).then {
        it.handleTie(amount, accidental, chord, eventAddress, endAddress)
      }
    } ?: Right(this)
  }

  private fun Score.doHandleChord(
    amount: Int,
    accidental: Accidental?,
    chord: Chord,
    eventAddress: EventAddress,
  ): ScoreResult {

    val shifted = chord.notes.withIndex().map { iv ->
      if (eventAddress.id == 0 || eventAddress.id == iv.index + 1) {
        val acc = accidental ?: iv.value.pitch.accidental
        iv.value.shift(amount, acc)
      } else {
        iv.value
      }
    }
    val newChord = chord.copy(notes = shifted).toEvent()
    return DurationSubAdder.addEvent(
      this, EventDestination(listOf(ScoreLevelType.VOICEMAP)),
      EventType.DURATION, newChord.params, eventAddress.idless()
    )
  }


  private fun Note.shift(amount: Int, accidental: Accidental): Note {
    return if (pitch == unPitched() || pitch.midiVal + amount <= 0) {
      this
    } else {
      val pitch = getNoteShift(pitch, amount, accidental)
       copy(pitch = pitch)
    }
  }

  private fun Score.handleTie(
    amount: Int, accidental: Accidental?,
    chord: Chord, eventAddress: EventAddress,
    endAddress: EventAddress?
  ): ScoreResult {
    return getAllTiedChords(
      eventAddress,
      endAddress,
      chord
    ).filter { endAddress == null || !(it.second.horizontal >= eventAddress.horizontal && it.second.horizontal <= endAddress.horizontal) }
      .foldRight(Right(this) as ScoreResult) { (c, ea), res ->
        doHandleChord(amount, accidental, c, ea)
      }

  }

  private fun Score.getAllTiedChords(
    eventAddress: EventAddress,
    endAddress: EventAddress?,
    chord: Chord
  ): List<Pair<Chord, EventAddress>> {
    return chord.notes.withIndex().flatMap {
      listOfNotNull(
        getTiePrevious(it.value, eventAddress.copy(id = it.index + 1)),
        getTieNext(it.value, eventAddress.copy(id = it.index + 1))
      )
    }
  }

  private fun Score.getTiePrevious(
    note: Note,
    eventAddress: EventAddress
  ): Pair<Chord, EventAddress>? {
    return if (note.isEndTie && note.endTie > dZero()) {
      subtractDuration(eventAddress, note.endTie)?.let { previous ->
        getSameNote(previous, note)?.let { it to previous }
      }
    } else null
  }

  private fun Score.getTieNext(
    note: Note,
    eventAddress: EventAddress
  ): Pair<Chord, EventAddress>? {
    return if (note.isStartTie) {
      addDuration(eventAddress, note.duration)?.let { next ->
        getSameNote(next, note)?.let { it to next }
      }
    } else null
  }


  private fun Score.getSameNote(eventAddress: EventAddress, note: Note): Chord? {
    return getEvent(EventType.DURATION, eventAddress.idless())?.let { chord(it) }?.let { chord ->
      if (chord.notes.any { it.pitch.midiVal == note.pitch.midiVal }) chord else null
    }
  }
}

