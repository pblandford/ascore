package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.*

object BarLineSubAdder : BaseSubAdder {

    override fun addEvent(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        params: ParamMap,
        eventAddress: EventAddress
    ): ScoreResult {

        return params.g<BarLineType>(EventParam.TYPE)?.let { type ->
            when (type) {
                BarLineType.START_REPEAT ->
                    super.addEvent(score, destination, EventType.REPEAT_START, params, eventAddress)

                BarLineType.END_REPEAT ->
                    super.addEvent(score, destination, EventType.REPEAT_END, params, eventAddress)

                else ->
                    super.deleteEvent(
                        score,
                        destination,
                        EventType.REPEAT_END,
                        params,
                        eventAddress
                    )
                        .then {
                            super.deleteEvent(
                                it,
                                destination,
                                EventType.REPEAT_START,
                                params,
                                eventAddress.inc()
                            )
                        }.then {
                            if (params[EventParam.TYPE] == BarLineType.NORMAL) {
                                super.deleteEvent(
                                    it,
                                    destination,
                                    EventType.BARLINE,
                                    params,
                                    eventAddress
                                )
                            } else {
                                super.addEvent(it, destination, eventType, params, eventAddress)
                            }
                        }
            }
        } ?: Left(ParamsMissing(listOf(EventParam.TYPE)))
    }

    override fun deleteEvent(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        params: ParamMap,
        eventAddress: EventAddress
    ): ScoreResult {
        return super.deleteEvent(score, destination, eventType, params, eventAddress)
            .then {
                GenericSubAdder.deleteEvent(
                    it,
                    destination,
                    EventType.REPEAT_START,
                    params,
                    eventAddress.inc()
                )
            }
            .then {
                GenericSubAdder.deleteEvent(
                    it,
                    destination,
                    EventType.REPEAT_END,
                    params,
                    eventAddress
                )
            }.then {
                if (eventAddress.barNum == 1) {
                    GenericSubAdder.deleteEvent(
                        it,
                        destination,
                        EventType.REPEAT_START,
                        params,
                        eventAddress
                    )
                } else {
                    it.ok()
                }
            }
    }
}