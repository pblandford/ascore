package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Bar
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.VoiceMap
import com.philblandford.kscore.engine.core.score.voiceMap
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.eventadder.EventDestination
import com.philblandford.kscore.engine.eventadder.BaseSubAdder
import com.philblandford.kscore.engine.eventadder.Right
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.eventadder.VoiceMapResult
import com.philblandford.kscore.engine.eventadder.mapOrFail
import com.philblandford.kscore.engine.eventadder.scoreDestination
import com.philblandford.kscore.engine.eventadder.then
import com.philblandford.kscore.engine.eventadder.util.tidy
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.time.getRegularDivisions
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.ez

object HiddenTimeSignatureSubAdder : BaseSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    val timeSignature = TimeSignature.fromParams(params + (EventParam.HIDDEN to true))
    return super.addEvent(
        score,
        scoreDestination,
        eventType,
        params,
        eventAddress
      ).then {
        it.transformBars(eventAddress.barNum, eventAddress.barNum) { _, _, _, _ ->
          voiceMaps.mapOrFail { it.adaptToHidden(timeSignature) }
            .then { vms ->
              Right(replaceSelf(eventMap, vms) as Bar)
            }
        }
    }
  }
}

private fun VoiceMap.adaptToHidden(timeSignature: TimeSignature): VoiceMapResult {
  val events = eventMap.deleteEvent(ez(0, timeSignature.duration), EventType.DURATION)
  return voiceMap(timeSignature, events).tidy()
    .then { it.fillRestsForHidden() }
}


fun VoiceMap.fillRestsForHidden(): VoiceMapResult {
  return if (getEvents(EventType.DURATION)?.isEmpty() != false) {
    val events = timeSignature.getRegularDivisions().fold(eventMap) { em, (o, div) ->
      em.putEvent(ez(0, o), rest(div))
    }
    Right(voiceMap(timeSignature, events))
  } else Right(this)
}





