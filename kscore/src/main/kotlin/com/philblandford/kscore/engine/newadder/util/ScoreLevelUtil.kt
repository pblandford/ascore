package com.philblandford.kscore.engine.newadder.util

import com.philblandford.kscore.engine.core.score.ScoreLevel
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventType

fun <T : ScoreLevel> T.putEvent(eventAddress: EventAddress, event: Event): T {
  val newMap = eventMap.putEvent(eventAddress, event)
  return replaceSelf(newMap) as T
}

fun <T : ScoreLevel> T.putEventAtScoreLevel(
  scoreLevelType: ScoreLevelType,
  eventAddress: EventAddress,
  event: Event
): ScoreLevelResult {
  return getScoreLevel(scoreLevelType, eventAddress).then {
    val newSl = it.putEvent(eventAddress.strip(scoreLevelType, event.eventType), event)
    replaceScoreLevel(newSl, eventAddress)
  }
}

fun ScoreLevel.getNote(eventAddress: EventAddress, midiVal: Int): Pair<EventAddress, Note>? {

  return getEvent(EventType.DURATION, eventAddress.idless())?.let { chord(it) }?.let { chord ->
    chord.notes.withIndex().find { it.value.pitch.midiVal == midiVal }?.let { iv ->
      eventAddress.copy(id = iv.index + 1) to iv.value
    }
  }
}

fun ScoreLevel.replaceScoreLevel(
  scoreLevel: ScoreLevel,
  eventAddress: EventAddress
): AnyResult<ScoreLevel> {
  return if (scoreLevel.scoreLevelType == subLevelType) {
    val newThis = replaceSubLevel(scoreLevel, subLevelIdx(eventAddress))
    Right(newThis)
  } else {
    getSubLevel(eventAddress)?.replaceScoreLevel(scoreLevel, eventAddress)?.then {
      replaceScoreLevel(it, eventAddress)
    } ?: Left(NotFound("Scorelevel $subLevelType not found at $eventAddress"))
  }
}

fun ScoreLevel.getScoreLevel(
  scoreLevel: ScoreLevelType,
  eventAddress: EventAddress
): ScoreLevelResult {
  return getSubLevel(eventAddress)?.let { sl ->
    if (sl.scoreLevelType == scoreLevel) {
      Right(sl)
    } else {
      sl.getScoreLevel(scoreLevel, eventAddress)
    }
  } ?: Left(NotFound("Could not find $scoreLevel at $eventAddress"))
}