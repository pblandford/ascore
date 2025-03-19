package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.ea

object GlissandoSubAdder : LineSubAdderIf {

    override fun addEvent(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        params: ParamMap,
        eventAddress: EventAddress
    ): ScoreResult {

        return super.addEvent(score, destination, eventType, params, eventAddress).otherwise {
            val nextSegment = score.getNextStaveSegment(eventAddress) ?: run {
                EventAddress(score.oLookup.numBars+1, staveId = eventAddress.staveId)
            }
            val newParams = params.plus(EventParam.END to nextSegment)
            super.addEvent(score, destination, eventType, newParams, eventAddress)
        }
    }

    override fun adjustAddress(eventAddress: EventAddress, params: ParamMap): EventAddress {
        return eventAddress
    }
}