package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.part
import com.philblandford.kscore.engine.core.score.stave
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.util.replace

object StaveSubAdder : BaseEventAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return params.g<List<ClefType>>(EventParam.CLEF).ifNullError { clefs ->
      score.getPart(eventAddress.staveId.main).ifNullError { part ->
        val diff =  clefs.size - part.staves.size
        val newPart = if (diff > 0) {
          val newStaves = (1..diff).map { stave(clefs[it + part.staves.size - 1],
            part.getNumBars(), score.getTimeSignature(ez(1))!!) }
          part(part.staves + newStaves, part.eventMap)
        } else {
          part(part.staves.dropLast(-diff), part.eventMap)
        }
        score.copy(parts = score.parts.replace(part, newPart)).ok()
      }
    }
  }
}