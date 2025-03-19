package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.EventDestination
import com.philblandford.kscore.engine.eventadder.BaseSubAdder
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.types.*

object LyricSubAdder : BaseSubAdder {

    override fun addEvent(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        params: ParamMap,
        eventAddress: EventAddress
    ): ScoreResult {

        val num = params.g<Int>(EventParam.NUMBER) ?: 1
        val address = eventAddress.copy(id = num, voice = 1)
        val newParams = params.g<LyricType>(EventParam.TYPE)?.let { params } ?: params.plus(
            EventParam.TYPE to LyricType.ALL
        )
        return super.addEvent(score, destination, eventType, newParams, address)
    }

    override fun deleteEvent(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        params: ParamMap,
        eventAddress: EventAddress
    ): ScoreResult {
        return super.deleteEvent(
            score,
            destination,
            eventType,
            params,
            eventAddress.copy(voice = 1)
        )
    }

    override fun addEventRange(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        params: ParamMap,
        eventAddress: EventAddress,
        endAddress: EventAddress
    ): ScoreResult {

        return super.addEventRange(
            score,
            destination,
            eventType,
            params,
            eventAddress.copy(voice = 1),
            endAddress.copy(voice = 1)
        )
    }

    override fun deleteEventRange(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        eventAddress: EventAddress,
        endAddress: EventAddress
    ): ScoreResult {

        return super.deleteEventRange(
            score,
            destination,
            eventType,
            eventAddress.copy(voice = 1),
            endAddress.copy(voice = 1)
        )
    }
}