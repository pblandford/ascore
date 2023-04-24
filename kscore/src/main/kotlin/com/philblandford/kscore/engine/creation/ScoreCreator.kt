package com.philblandford.kscore.engine.creation

import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.NewScoreDescriptor
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.dsl.score
import com.philblandford.kscore.engine.eventadder.NewEventAdder
import com.philblandford.kscore.engine.eventadder.rightOrThrow
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.EventType.HIDDEN_TIME_SIGNATURE
import com.philblandford.kscore.engine.types.EventType.UISTATE

class ScoreCreator(private val instrumentGetter: InstrumentGetter) {

  fun createDefault(numBars: Int = 32, pageSize: PageSize = PageSize.A5): Score {
    return Score.create(instrumentGetter, numBars, pageSize = pageSize)
  }

  fun createScore(nsd: NewScoreDescriptor): Score {
    var score = score(nsd.keySignature, nsd.timeSignature, nsd.tempo, nsd.meta,  nsd.pageSize) {
      nsd.instruments.forEach { instr ->
        part(instr) {
          instr.clefs.forEach {
            stave(it) {
              (1..nsd.numBars).forEach { _ ->
                bar {
                  voiceMap { }
                }
              }
            }
          }
        }
      }
    }
    if (nsd.upbeatEnabled) {
      score =
        NewEventAdder.addEvent(
          score, HIDDEN_TIME_SIGNATURE,
          nsd.upBeat.copy(hidden = true).toEvent().params,
          ez(1)
        ).rightOrThrow()
    }
    score = NewEventAdder.addEvent(
      score,
      UISTATE,
      paramMapOf(EventParam.MARKER_POSITION to ea(1)),
      eZero()
    ).rightOrThrow()
    return score
  }

}