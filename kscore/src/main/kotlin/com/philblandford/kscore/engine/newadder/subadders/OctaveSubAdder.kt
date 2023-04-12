package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Bar
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.VoiceMap
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.newadder.util.changeSubLevel
import com.philblandford.kscore.engine.types.*

internal object OctaveSubAdder : LineSubAdderIf {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return params.g<Int>(EventParam.NUMBER)?.let { num ->
      val finalParams = params.plus(EventParam.IS_UP to (num > 0))
      super.addEvent(score, destination, eventType, finalParams, eventAddress)
        .then { it.setNotes(params, eventAddress) }
    } ?: Left(ParamsMissing(listOf(EventParam.NUMBER)))
  }

  override fun adjustAddress(eventAddress: EventAddress, params: ParamMap): EventAddress {
    return eventAddress
  }

  private fun Score.setNotes(params: ParamMap, eventAddress: EventAddress): ScoreResult {
    return getEnd(params, eventAddress).then { end ->
      getStave(eventAddress.staveId)?.let { stave ->
          stave.transformBars(eventAddress, end, eventAddress.staveId) { so, eo, _, _ ->
            val vms = voiceMaps.map { it.setNotes(params, so, eo) }
            Right(Bar(timeSignature, vms, eventMap))
          }.then {
            changeSubLevel(it, eventAddress)
          }
      } ?: Left(NotFound("Stave not found"))
    }
  }

  private fun Score.getEnd(params: ParamMap, eventAddress: EventAddress): AnyResult<EventAddress> {
    return (params.g<EventAddress>(EventParam.END) ?:
        params.g<Duration>(EventParam.DURATION)?.let {  duration ->
          addDuration(eventAddress, duration)
        })?.let { Right(it) } ?: Left(NotFound("End of octave line not found"))
  }

  private fun VoiceMap.setNotes(params: ParamMap, start: Offset?, end: Offset?): VoiceMap {
    val num = params.g<Int>(EventParam.NUMBER) ?: 0
    return transformEvents(start, end) {
      chord(this)?.transformNotes { note ->
        note.copy(pitch = note.pitch.copy(octave = note.pitch.octave + num))
      }?.toEvent() ?: this
    }
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.setNotesDeleted(eventAddress)
      .then {
        super.deleteEvent(it, destination, eventType, params, eventAddress)
      }
  }

  private fun Score.setNotesDeleted(eventAddress: EventAddress): ScoreResult {
    return getEventAt(EventType.OCTAVE, eventAddress)?.let { event ->
      event.second.getParam<Duration>(EventParam.DURATION)?.let { duration ->
        val number = event.second.getInt(EventParam.NUMBER)
        addDuration(eventAddress, duration)?.let { end ->
          setNotes(paramMapOf(EventParam.NUMBER to -number, EventParam.END to end), eventAddress)
        }
      }
    } ?: Left(NotFound("Octave not found at $eventAddress"))
  }

}