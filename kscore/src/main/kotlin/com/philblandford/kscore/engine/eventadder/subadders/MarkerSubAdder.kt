package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.duration.plus
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.*

internal object MarkerSubAdder : BaseSubAdder {

  override val addListeners: Map<EventType, AddListener> =
    mapOf(
      EventType.STAVE to { score, _, _, _, ea ->
        handleStaveChange(score, ea)
      },
    )

  override val deleteListeners:Map<EventType, DeleteListener> = mapOf(
    EventType.PART to { score, _, _, _, ea ->
      handlePartChange(score, ea)
    },
  )

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return if (!params.isTrue(EventParam.HOLD)) {
      score.getNextSegmentAdd(eventAddress, params).ifNullRestore(score) { next ->
        doSetMarker(score, next)
      }
    } else {
      score.ok()
    }
  }

  private fun doSetMarker(score: Score, eventAddress: EventAddress): ScoreResult {
    return GenericSubAdder.setParam(
      score, EventDestination(listOf(ScoreLevelType.SCORE)),
      EventType.UISTATE, EventParam.MARKER_POSITION, eventAddress, eZero()
    )
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return score.getNextSegmentDelete(eventAddress)?.let { next ->
      GenericSubAdder.setParam(
        score, EventDestination(listOf(ScoreLevelType.SCORE)),
        EventType.UISTATE, EventParam.MARKER_POSITION, next, eZero()
      )
    } ?: Right(score)
  }

  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    return GenericSubAdder.setParam(
      score, EventDestination(listOf(ScoreLevelType.SCORE)),
      EventType.UISTATE, EventParam.MARKER_POSITION, value, eZero()
    )
  }

  private fun Score.getNextSegmentAdd(eventAddress: EventAddress, params: ParamMap): EventAddress? {
    return eventAddress.graceOffset?.let { go ->
      getEvent(EventType.DURATION, eventAddress)?.let { event ->
        val nextGrace = eventAddress.copy(graceOffset = go + event.duration(), voice = 0)
        if (getEvent(EventType.DURATION, nextGrace) == null) {
          eventAddress.graceless().voiceless()
        } else {
          nextGrace
        }
      }
    } ?: run {
      if ((params.g<GraceType>(EventParam.GRACE_TYPE) ?: GraceType.NONE) != GraceType.NONE) {
        eventAddress.voiceless()
      } else {
        if (eventAddress.voice == 0) {
          getNextStaveSegment(eventAddress)
        } else {
          getNextVoiceSegment(eventAddress)?.voiceIdless()
        }
      }
    }
  }

  private fun Score.getNextSegmentDelete(eventAddress: EventAddress): EventAddress? {
    return if (eventAddress.isGrace) {
      if (haveStaveSegment(eventAddress)) {
        /* If a grace note has been deleted, the others have shuffled back, so the new marker
       * position will be the same
       */
        eventAddress
      } else {
        /* Deleted the last grace note at this offset */
        eventAddress.graceless()
      }
    } else {
      if (eventAddress.voice == 0) {
        getNextStaveSegment(eventAddress)
      } else {
        getNextVoiceSegment(eventAddress)?.copy(voice = 0)
      }
    }?.voiceless()
  }

  private fun handleStaveChange(score: Score, eventAddress: EventAddress): ScoreResult {
    return score.getMarker()?.let { marker ->
      score.getPart(eventAddress.staveId.main)?.let { part ->
        if (marker.staveId.sub >= part.staves.size) {
          doSetMarker(
            score,
            eventAddress.copy(staveId = StaveId(eventAddress.staveId.main, part.staves.size))
          )
        } else score.ok()
      }
    } ?: score.ok()
  }

  private fun handlePartChange(score: Score, eventAddress: EventAddress): ScoreResult {
    return score.getMarker()?.let { marker ->
      if (marker.staveId.main == eventAddress.staveId.main && eventAddress.staveId.main > score.parts.size) {
        doSetMarker(score, eventAddress.copy(staveId = StaveId(score.parts.size, 1)))
      } else {
        score.ok()
      }
    } ?: score.ok()
  }
}