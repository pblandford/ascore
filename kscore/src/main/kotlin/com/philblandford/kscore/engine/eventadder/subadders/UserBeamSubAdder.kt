package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.representation.MAX_VOICE
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.duration.minus
import com.philblandford.kscore.engine.duration.plus
import com.philblandford.kscore.engine.eventadder.AddListener
import com.philblandford.kscore.engine.eventadder.EventDestination
import com.philblandford.kscore.engine.eventadder.Failure
import com.philblandford.kscore.engine.eventadder.Left
import com.philblandford.kscore.engine.eventadder.ParamsMissing
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.eventadder.fold
import com.philblandford.kscore.engine.eventadder.ok
import com.philblandford.kscore.engine.eventadder.partDestination
import com.philblandford.kscore.engine.eventadder.then
import com.philblandford.kscore.engine.types.DurationType
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.StaveId
import com.philblandford.kscore.engine.types.g
import com.philblandford.kscore.engine.types.paramMapOf

internal object UserBeamSubAdder : LineSubAdderIf {

    override val deleteListeners: Map<EventType, AddListener> =
        mapOf(
            EventType.DURATION to { s, d, _, _, e -> onDurationDelete(s, d, e) },
        )

    override fun addEvent(
        score: Score,
        destination: EventDestination,
        eventType: EventType,
        params: ParamMap,
        eventAddress: EventAddress
    ): ScoreResult {
        val endParam = params.g<EventAddress>(EventParam.END)
        val end = endParam ?: params.g<Duration>(EventParam.DURATION)
            ?.let { score.addDuration(eventAddress, it) }
        return end?.let {
            val durationEvents = score.getEvents(EventType.DURATION, eventAddress, end)?.toList()
                ?.sortedBy { it.first.eventAddress } ?: listOf()
            var usefulEvents = durationEvents.dropWhile {
                it.second.subType != DurationType.CHORD
                        || it.first.eventAddress.isGrace
            }
                .dropLastWhile { it.second.subType != DurationType.CHORD || it.first.eventAddress.isGrace }
            usefulEvents = usefulEvents.filterNot { it.second.duration() >= crotchet() }
            if (usefulEvents.size < 2) {
                score.ok()
            } else {
                usefulEvents.firstOrNull()?.first?.eventAddress?.let { realStart ->
                    usefulEvents.lastOrNull()?.first?.eventAddress?.let { realEnd ->
                        if (realStart != realEnd) {
                            val realParams = params + (EventParam.END to realEnd)
                            super.addEvent(score, destination, eventType, realParams, realStart)
                                .then {
                                    it.refreshBeams()
                                }
                        } else {
                            score.ok()
                        }
                    }
                }
            }
        } ?: Left(ParamsMissing(listOf(EventParam.END)))
    }

    override fun EventAddress.adjustForDestination(): EventAddress {
        return copy(staveId = StaveId(0, staveId.sub), id = 0)
    }

    private fun onDurationDelete(
        score: Score,
        destination: EventDestination,
        eventAddress: EventAddress
    ): ScoreResult {
        val eventOffset = score.addressToOffset(eventAddress) ?: dZero()

        return score.fold(
            score.parts[eventAddress.staveId.main - 1].getEvents(EventType.BEAM)?.toList()
                ?.filterNot { it.second.isTrue(EventParam.END) } ?: listOf()
        ) { (key, beam) ->
            val startOffset = addressToOffset(key.eventAddress) ?: dZero()
            val endOffset = startOffset + beam.duration()
            if (eventOffset == endOffset) {
                handleDeleteEndBeam(
                    key.eventAddress.copy(staveId = eventAddress.staveId),
                    startOffset,
                    endOffset,
                    beam
                )
            } else if (key.eventAddress.horizontal == eventAddress.horizontal) {
                handleDeleteStartBeam(
                    key.eventAddress.copy(staveId = eventAddress.staveId),
                    startOffset,
                    beam
                )
            } else {
                ok()
            }
        }
    }

    private fun Score.handleDeleteEndBeam(
        eventAddress: EventAddress, startOffset: Offset, endOffset: Offset, beamEvent: Event
    ): ScoreResult {
        return offsetToAddress(endOffset)?.let { endAddress ->
            getPreviousStaveSegment(endAddress.copy(staveId = eventAddress.staveId))?.let { previous ->
                val newDuration = (addressToOffset(previous) ?: dZero()) - startOffset
                deleteEvent(
                    this,
                    partDestination,
                    EventType.BEAM,
                    paramMapOf(),
                    eventAddress
                ).then {
                    addEvent(
                        it,
                        partDestination,
                        EventType.BEAM,
                        beamEvent.params + (EventParam.DURATION to newDuration),
                        eventAddress
                    )
                }
            }
        } ?: ok()
    }

    private fun Score.handleDeleteStartBeam(
        eventAddress: EventAddress, startOffset: Offset, beamEvent: Event
    ): ScoreResult {

        val end = startOffset + beamEvent.duration()

        return getNextStaveSegment(eventAddress)?.let { next ->
            val newDuration = end - (addressToOffset(next) ?: dZero())

            deleteEvent(
                this,
                partDestination,
                EventType.BEAM,
                paramMapOf(),
                eventAddress
            ).then { s2 ->
                if (newDuration > dZero()) {
                    addEvent(
                        s2,
                        partDestination,
                        EventType.BEAM,
                        beamEvent.params + (EventParam.DURATION to newDuration),
                        eventAddress.copy(offset = next.offset)
                    )
                } else s2.ok()
            }
        } ?: ok()
    }


}





