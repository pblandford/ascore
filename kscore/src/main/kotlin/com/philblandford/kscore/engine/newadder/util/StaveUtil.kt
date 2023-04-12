package com.philblandford.kscore.engine.newadder.util

import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.core.score.Stave
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.kscore.engine.types.ez

fun Stave.setTies(scoreQuery: ScoreQuery, startBar: Int = 1, endBar: Int = numBars): StaveResult {
  return bars.subList(startBar - 1, endBar).fold(Right(this) as StaveResult) { sr, bar ->
    val notes = bar.getNotes().filter { it.value.isStartTie }.toList()
    notes.fold(sr) { sr2, (ea, note) ->
      sr2.then {
        it.setTiesForNote(
          scoreQuery,
          note,
          ez(1, ea.offset).copy(graceOffset = ea.graceOffset)
        )
      }
    }
  }
}

fun Stave.setTiesForNote(
  scoreQuery: ScoreQuery,
  note: Note,
  eventAddress: EventAddress
): StaveResult {
  return scoreQuery.addDuration(eventAddress.voiceIdless(), note.duration)?.let { endAddress ->
    getNote(endAddress, note.pitch.midiVal)?.let { (endNoteAddress, endNote) ->
      val newNote = endNote.copy(isEndTie = true, endTie = note.duration)
      replaceNote(newNote, endNoteAddress)
    }
  } ?: Warning(NotFound("Could not get end note"), this)
}


fun Stave.replaceNote(note: Note, eventAddress: EventAddress): StaveResult {
  return getEvent(EventType.DURATION, eventAddress.idless())?.let { chord(it) }?.let { chord ->
    val event = chord.replaceNote(eventAddress.id - 1, note).toEvent()
    putEventAtScoreLevel(ScoreLevelType.VOICEMAP, eventAddress.idless(), event).then { sl ->
      Right(sl as Stave)
    }
  } ?: Left(NotFound("Chord not found at $eventAddress"))
}