package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.Tuplet
import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.core.score.voiceMap
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.newadder.subadders.DurationSubAdder.deleteDurationEvent
import com.philblandford.kscore.engine.newadder.util.changeSubLevel
import com.philblandford.kscore.engine.newadder.util.getOrCreateVoiceMap
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLogt

object TupletSubAdder : NewSubAdder {
    override fun addEvent(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        params: ParamMap,
        eventAddress: EventAddress
    ): ScoreResult {
        score.getTuplet(eventAddress)?.let {
            return Warning(HarmlessFailure("Tuplet already at $eventAddress"), score)
        }

        return score.createTuplet(params, eventAddress).then { tuplet ->
            score.addMarker(tuplet, eventAddress, destination).then { newScore ->
                newScore.addTuplet(tuplet, eventAddress)
            }
        }
    }

    override fun <T> setParam(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        param: EventParam,
        value: T,
        eventAddress: EventAddress
    ): ScoreResult {
        return if (param == EventParam.HARD_START) {
            val coord = (value as? Coord) ?: Coord()
            score.getVoiceMap(eventAddress).ifNullFail("Voice map not found").then { vm ->
                vm.getTuplet(eventAddress.offset).ifNullFail("Tuplet not found").then { tuplet ->
                    score.addTuplet(tuplet.copy(hardStart = coord), eventAddress)
                }
            }
        } else score.ok()
    }

    override fun addEventRange(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        params: ParamMap,
        eventAddress: EventAddress,
        endAddress: EventAddress
    ): ScoreResult {
        var events =
            score.getEvents(EventType.DURATION, eventAddress, endAddress)?.toList() ?: listOf()
        val wholeRests = (eventAddress.barNum..endAddress.barNum).flatMap { bar ->
            score.getStaveRange(eventAddress.staveId, endAddress.staveId).mapNotNull { stave ->
                val addr = eas(bar, dZero(), stave).copy(voice = eventAddress.voice)
                if (score.getEvent(EventType.DURATION, addr) == null) {
                    EventMapKey(EventType.DURATION, addr) to rest(
                        score.getTimeSignature(addr)?.duration ?: semibreve()
                    )
                } else {
                    null
                }
            }
        }
        events = events.plus(wholeRests)
        ksLogt("Fuck you ${events}")
        return events.toList().fold(Right(score) as ScoreResult) { sr, (k, v) ->
            sr.then { addEvent(it, destination, eventType, params, k.eventAddress) }
        }
    }


    override fun deleteEvent(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        params: ParamMap,
        eventAddress: EventAddress
    ): ScoreResult {

        return score.getVoiceMap(eventAddress)?.let { vm ->
            vm.subLevels.find { it.offset == eventAddress.offset }?.let { tuplet ->
                vm.deleteDurationEvent(eventAddress, params)
                    .then {
                        val newSubLevels = it.subLevels.minus(tuplet)
                        val newVm = voiceMap(it.eventMap, newSubLevels)
                        score.changeSubLevel(newVm, eventAddress)
                    }
            }
        } ?: Right(score)
    }

    override fun deleteEventRange(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        eventAddress: EventAddress,
        endAddress: EventAddress
    ): ScoreResult {
        val victims =
            score.getEvents(EventType.TUPLET, eventAddress, endAddress)?.filterNot { (key, event) ->
                event.isTrue(EventParam.END) ||
                        (key.eventAddress.barNum == endAddress.barNum &&
                                key.eventAddress.offset + event.realDuration() > endAddress.offset &&
                                !event.endIsLastMember(eventAddress, endAddress)
                                )
            }?.toList() ?: listOf()
        return victims.fold(Right(score) as ScoreResult) { sr, (key, _) ->
            sr.then {
                deleteEvent(it, destination, eventType, paramMapOf(), key.eventAddress)
            }
        }
    }

    private fun Event.endIsLastMember(
        eventAddress: EventAddress,
        endAddress: EventAddress
    ): Boolean {
        return getParam<List<Duration>>(EventParam.MEMBERS)?.let { members ->
            members.lastOrNull()?.let { last ->
                eventAddress.offset + last == endAddress.offset
            }
        } ?: false
    }

    private fun Score.addMarker(
        tuplet: Tuplet,
        eventAddress: EventAddress,
        destination: EventDestination
    ): ScoreResult {
        return DurationSubAdder.addEvent(
            this, destination, EventType.DURATION,
            paramMapOf(
                EventParam.TYPE to DurationType.TUPLET_MARKER,
                EventParam.DURATION to tuplet.realDuration,
                EventParam.HOLD to true
            ),
            eventAddress
        )
    }

    private fun Score.addTuplet(tuplet: Tuplet, eventAddress: EventAddress): ScoreResult {
        val voiceMap = getOrCreateVoiceMap(eventAddress)
        val sls = voiceMap.subLevels.filterNot { it.offset == tuplet.offset }.plus(tuplet)
        val newVm = voiceMap.replaceSelf(voiceMap.eventMap, sls)
        return changeSubLevel(newVm, eventAddress)
    }

    private fun Score.createTuplet(
        params: ParamMap,
        eventAddress: EventAddress
    ): AnyResult<Tuplet> {
        val hidden = params.isTrue(EventParam.HIDDEN)
        return params.g<Int>(EventParam.NUMERATOR)?.let { numerator ->
            params.g<Duration>(EventParam.DURATION)?.let { duration ->
                Right(tuplet(eventAddress.offset, numerator, duration, hidden))
            } ?: params.g<Int>(EventParam.DENOMINATOR)?.let { denominator ->
                Right(tuplet(eventAddress.offset, numerator, denominator, hidden))
            } ?: run {
                val duration =
                    getParam<Duration>(EventType.DURATION, EventParam.DURATION, eventAddress)
                        ?: getTimeSignature(eventAddress)?.duration
                duration?.let {
                    Right(tuplet(eventAddress.offset, numerator, duration, hidden))
                }
            }
        } ?: Left(ParamsMissing(listOf(EventParam.DURATION, EventParam.NUMERATOR)))
    }
}