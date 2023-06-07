package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.representation.MAX_VOICE
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.AddListener
import com.philblandford.kscore.engine.eventadder.BaseSubAdder
import com.philblandford.kscore.engine.eventadder.EventDestination
import com.philblandford.kscore.engine.eventadder.RangeListener
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap

object BeamSubAdder : BaseSubAdder {

  override val addListeners: Map<EventType, AddListener> =
    mapOf(
      EventType.CLEF to ::handleRangeChange,
      EventType.DURATION to ::handleDurationEventChange,
      EventType.NOTE to ::handleDurationEventChange,
      EventType.NOTE_SHIFT to ::handleDurationEventChange
    )

  override val deleteListeners: Map<EventType, AddListener> =
    mapOf(
      EventType.CLEF to ::handleRangeChange,
      EventType.DURATION to ::handleDurationEventChange,
      EventType.NOTE to ::handleDurationEventChange,
    )

  override val rangeListeners:Map<EventType, RangeListener> =
    mapOf(EventType.DURATION to ::handleRangeSetChange,
      EventType.NOTE_SHIFT to ::handleRangeSetChange
    )


  private fun handleDurationEventChange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.updateBeams(eventAddress.voiceIdless(), eventAddress.voiceIdless())
  }

  private fun handleRangeChange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.updateBeams(eventAddress, eventAddress.copy(score.numBars))
  }

  private fun handleRangeSetChange(
    score:Score,
    destination: EventDestination,
    eventType: EventType,
    params:ParamMap,
    start:EventAddress,
    end:EventAddress
  ):ScoreResult {
    return score.updateBeams(start, end)
  }

  private fun Score.updateBeams(start: EventAddress, end: EventAddress): ScoreResult {
    val addresses = (start.barNum..end.barNum).flatMap { bar ->
      (1..MAX_VOICE).map { voice -> start.copy(barNum = bar, voice = voice) }
    }
    val beamDirectory = this.beamDirectory.update(this, addresses)
    val newScore = this.copy(beamDirectory = beamDirectory)
    return newScore.beamDirectory.markBeamGroupMembers(
      newScore,
      start.barNum,
      end.barNum,
      listOf(start.staveId)
    )
  }
}