package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.Stave
import com.philblandford.kscore.engine.core.score.VoiceMap
import com.philblandford.kscore.engine.core.score.VoiceNumberMap
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.newadder.util.changeSubLevel
import com.philblandford.kscore.engine.newadder.util.setAllPositions
import com.philblandford.kscore.engine.newadder.util.setStemDirection
import com.philblandford.kscore.engine.types.*

object ClefSubAdder : RangeSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    if (score.getInstrument(eventAddress)?.percussion == true) {
      return score.ok()
    }

    return deleteEvent(score, destination, eventType, params, eventAddress).then { removedScore ->

      if (removedScore.getEventAt(EventType.CLEF, eventAddress)?.second?.subType !=
        params[EventParam.TYPE]
      ) {
         params.g<ClefType>(EventParam.TYPE)?.let { clefType ->
          when (val res = super.addEvent(removedScore, destination, eventType, params, eventAddress)) {
            is Right -> res.r.postAdd(clefType, eventAddress)
            else -> res
          }
        } ?: Left(Error("No clef type specified"))
      } else {
        Right(removedScore)
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
    if (eventAddress.isStart()) {
      return Warning(HarmlessFailure("Cannot delete first clef"), score)
    }
    return when (val res = super.deleteEvent(score, destination, eventType, params, eventAddress)) {
      is Right -> res.r.postDelete(eventAddress)
      else -> res
    }
  }

  private fun Score.postDelete(eventAddress: EventAddress): ScoreResult {
    return getEventAt(EventType.CLEF, eventAddress)?.let { lastClef ->
      postAdd(lastClef.second.subType as ClefType, eventAddress)
    } ?: Left(Error("No previous clef at $eventAddress"))
  }

  private fun Score.postAdd(clefType: ClefType, eventAddress: EventAddress): ScoreResult {
    return getStave(eventAddress.staveId)?.let { stave ->
      stave.setNotePositions(clefType, eventAddress).then {
        it.removeSameLater(EventType.CLEF, eventAddress) { subType == clefType }
      }.then {
        changeSubLevel(it, eventAddress)
      }
    } ?: Left(Error("Could not find stave"))
  }

  private fun Stave.setNotePositions(clefType: ClefType, eventAddress: EventAddress): StaveResult {
    val end = eventMap.getEventAfter(EventType.CLEF, eventAddress)?.first?.eventAddress
      ?: ez(numBars + 1)
    return transformBars(eventAddress, end, sZero()) { startOffset, endOffset, _, _ ->
      transformVoiceMaps { voice ->
        setNotePositions(startOffset, endOffset, clefType, voiceNumberMap, voice)
      }
    }
  }

  private fun VoiceMap.setNotePositions(
    startOffset: Offset?, endOffset: Offset?,
    clefType: ClefType, voiceNumberMap: VoiceNumberMap,
    voice: Voice
  ): VoiceMapResult {

    return transformDurationEvents(startOffset, endOffset) { key, event ->
      event.setNotes(clefType, key.eventAddress, voiceNumberMap, voice)
    }
  }

  private fun Event.setNotes(
    clefType: ClefType, eventAddress: EventAddress,
    voiceNumberMap: VoiceNumberMap, voice: Voice
  ): Event {
    val voiceOpt = voiceNumberMap[eventAddress.offset]?.let { if (it > 1) voice else null }
    return chord(this)?.let { chord ->
      val chordEvent = setAllPositions(
        chord.toEvent(), clefType, voiceOpt, 0, eventAddress.isGrace
      )
      setStemDirection(
        chordEvent, voiceNumberMap[eventAddress.offset] ?: 1,
        voice, eventAddress.isGrace
      )
    } ?: this
  }
}

