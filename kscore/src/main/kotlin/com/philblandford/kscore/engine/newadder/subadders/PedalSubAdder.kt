package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.newadder.EventDestination
import com.philblandford.kscore.engine.newadder.ScoreResult
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.StaveId

internal object PedalSubAdder : LineSubAdderIf {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    val address = eventAddress.copy(staveId = StaveId(eventAddress.staveId.main, score.numStaves(eventAddress.staveId.main)))
    return super.addEvent(score, destination, eventType, params, address)
  }

  override fun adjustAddress(eventAddress: EventAddress, params: ParamMap): EventAddress {
    return eventAddress
  }
}