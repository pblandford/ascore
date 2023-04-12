package com.philblandford.kscore.engine.types

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.engine.beam.BeamMap
import com.philblandford.kscore.engine.core.*
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarEndAreaPair
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarStartAreaPair
import com.philblandford.kscore.engine.core.areadirectory.header.HeaderArea
import com.philblandford.kscore.engine.core.areadirectory.preheader.PreHeaderArea
import com.philblandford.kscore.engine.core.areadirectory.segment.SegmentArea
import com.philblandford.kscore.engine.core.stave.PartArea
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.time.TimeSignature
import java.util.*


interface EventMap : EventGetter, EventPutter {
  override fun putEvent(eventAddress: EventAddress, event: Event): EventMap
  override fun deleteEvent(
    startAddress: EventAddress,
    eventType: EventType,
    endAddress: EventAddress?,
    spareMe: (EventType, EventAddress) -> Boolean
  ): EventMap


  override fun deleteAll(eventType: EventType): EventMap
  override fun deleteRange(
    start: EventAddress, end: EventAddress,
    spareMe: (EventType, EventAddress) -> Boolean
  ): EventMap

  override fun setParam(
    eventAddress: EventAddress,
    eventType: EventType,
    param: EventParam,
    value: Any?
  ): EventMap

  fun replaceEvents(eventType: EventType, eventHash: EventHash): EventMap
  fun getEventTypes(): Iterable<EventType>
  fun shiftEvents(from: Int, by: Int, condition: (EventMapKey) -> Boolean = { true }): EventMap
  fun getEventAfter(eventType: EventType, eventAddress: EventAddress): Pair<EventMapKey, Event>?
}

interface OffsetLookup {
  fun addDuration(address: EventAddress, duration: Duration): EventAddress?
  fun subtractDuration(address: EventAddress, duration: Duration): EventAddress?
  fun addressToOffset(address: EventAddress): Duration?
  fun offsetToAddress(offset: Duration): EventAddress?
  fun getDuration(from: EventAddress, to: EventAddress): Duration?
  val numBars: Int
}

interface ScoreQuery : EventGetter, BeamQuery, OffsetLookup {
  val numParts: Int
  fun getTitle(): String?
  fun getSubtitle(): String?
  fun getComposer(): String?
  fun getFilename(): String?
  fun numStaves(part: Int): Int
  fun allParts(selected: Boolean): Iterable<PartNum>
  fun singlePartMode(): Boolean
  fun selectedPartName(): String?
  fun selectedPart(): Int
  fun getAllStaves(selected: Boolean): Iterable<StaveId>
  fun getStaveRange(start: StaveId, end: StaveId): Iterable<StaveId>
  fun allBarAddresses(selected: Boolean): Iterable<EventAddress>
  fun numVoicesAt(eventAddress: EventAddress): Int
  fun getEventsForPart(id: Int, level: Boolean = true): EventHash
  fun getEventsForStave(
    staveId: StaveId,
    types: Iterable<EventType>,
    start: EventAddress? = null,
    end: EventAddress? = null,
  ): EventHash

  fun <T> getOption(option: EventParam): T?
  fun getEventsForBar(eventType: EventType, eventAddress: EventAddress): EventHash
  fun getTimeSignature(eventAddress: EventAddress): TimeSignature?
  fun getKeySignature(eventAddress: EventAddress, concert: Boolean = false): Int?
  fun getInstrument(eventAddress: EventAddress, adjustTranspose: Boolean = true): Instrument?
  fun getOctaveShift(eventAddress: EventAddress): Int
  fun getNoteDuration(eventAddress: EventAddress): Duration?
  fun getEventEnd(eventAddress: EventAddress): EventAddress?

  fun getSystemEvents(): EventHash
  fun getPreviousStaveSegment(eventAddress: EventAddress): EventAddress?
  fun getFloorStaveSegment(eventAddress: EventAddress): EventAddress?
  fun getNextStaveSegment(eventAddress: EventAddress): EventAddress?
  fun getPreviousVoiceSegment(eventAddress: EventAddress): EventAddress?
  fun getNextVoiceSegment(eventAddress: EventAddress): EventAddress?
  fun haveStaveSegment(eventAddress: EventAddress): Boolean
  fun getEmptyVoiceMaps(start: EventAddress? = null, end: EventAddress? = null): Iterable<EventAddress>
  fun getRepeatBars(): Lookup<RepeatBarType>
  fun isRepeatBar(eventAddress: EventAddress): Boolean
  fun isEmptyBar(eventAddress: EventAddress): Boolean
  fun getLastSegmentInDuration(address: EventAddress): EventAddress?
}

abstract class EventGetterOption

interface EventGetter {
  fun getAllEvents(options: List<EventGetterOption> = listOf()): EventHash
  fun getAllEvents(segment: EventAddress): EventHash
  fun getEvents(
    eventType: EventType, eventAddress: EventAddress? = null,
    endAddress: EventAddress? = null,
    options:List<EventGetterOption> = listOf()
  ): EventHash?

  fun collateEvents(
    eventTypes: Iterable<EventType>, eventAddress: EventAddress? = null,
    endAddress: EventAddress? = null
  ): EventHash?

  fun getEvent(eventType: EventType, eventAddress: EventAddress = eZero()): Event?
  fun getEventAt(
    eventType: EventType,
    eventAddress: EventAddress = eZero()
  ): Pair<EventMapKey, Event>?

  fun <T> getParam(
    eventType: EventType,
    eventParam: EventParam,
    eventAddress: EventAddress = eZero()
  ): T?

  fun <T> getParamAt(
    eventType: EventType,
    eventParam: EventParam,
    eventAddress: EventAddress = eZero()
  ): T?

}

interface EventPutter {
  fun putEvent(eventAddress: EventAddress, event: Event): EventPutter
  fun deleteEvent(
    startAddress: EventAddress,
    eventType: EventType,
    endAddress: EventAddress? = null,
    spareMe: (EventType, EventAddress) -> Boolean = { _, _ -> false }
  ): EventPutter

  fun deleteAll(eventType: EventType): EventPutter
  fun deleteRange(
    start: EventAddress, end: EventAddress,
    spareMe: (EventType, EventAddress) -> Boolean = { _, _ -> false }
  ): EventPutter

  fun setParam(
    eventAddress: EventAddress,
    eventType: EventType,
    param: EventParam,
    value: Any?
  ): EventPutter
}

interface BeamQuery {
  fun getBeams(start: EventAddress? = null, end: EventAddress? = null): BeamMap
}

typealias Lookup<T> = Map<EventAddress, T>
typealias SegmentBarLookup = Map<Int, SegmentLookup>
typealias SegmentGeogBarLookup = Map<Int, SegmentGeogLookup>
typealias SegmentLookup = Map<EventAddress, SegmentArea>
typealias SegmentStaveLookup = Map<StaveId, SegmentLookup>
typealias SegmentGeogLookup = Map<EventAddress, SegmentGeography>
typealias SegmentGeogStaveMap = Map<StaveId, SegmentGeogLookup>

fun <T> lookupOf() = mapOf<EventAddress, T>()

interface AreaDirectoryQuery {
  fun getSegmentGeogsForColumn(barNum: Int): SegmentGeogStaveMap?
  fun getAllSegmentGeogsByBar(): SortedMap<Int, SegmentGeogStaveMap>
  fun getSegmentsForStave(staveId: StaveId): SegmentLookup
  fun getAllPreHeaderGeogs(): Lookup<PreHeaderGeography>
  fun getAllPreHeaderAreas(): Lookup<PreHeaderArea>
  fun getAllHeaderAreas(): Lookup<HeaderArea>
  fun getAllHeaderGeogs(): Lookup<HeaderGeography>
  fun getAllBarStartAreas(): Lookup<BarStartAreaPair>
  fun getAllBarEndAreas(): Lookup<BarEndAreaPair>
  fun getAllBarStartGeogs(): Lookup<BarStartGeographyPair>
  fun getAllBarEndGeogs(): Lookup<BarEndGeographyPair>
  fun getLyricWidths(): Lookup<Int>
  fun getHarmonyWidths(): Lookup<Int>
  fun getFermataWidths(): Lookup<Int>
  fun getSegmentExtensions(): Lookup<Int>
}

interface GeographyXQuery {
  fun getSystemXGeographies(): Iterable<SystemXGeography>
}

interface PartQuery {
  fun getParts(): Map<EventAddress, PartArea>
}

interface GeographyYQuery {
  fun getSystemYGeographies(): Iterable<SystemYGeography>
  fun getPageGeographies(): Iterable<PageGeography>
}

interface StavePositionFinder {
  fun getSlicePosition(eventAddress: EventAddress): SlicePosition?
  fun getLastSlicePosition(): SlicePosition?
  fun getPreviousSlicePosition(eventAddress: EventAddress): Pair<EventAddress, SlicePosition>?
  fun getFirstSegmentGeography(): SegmentGeography?
  fun getLastSegmentGeography(): SegmentGeography?
  fun getStemGeography(eventAddress: EventAddress): StemGeography?
  fun getSegmentGeography(eventAddress: EventAddress): SegmentGeography?
  fun getVoiceGeography(eventAddress: EventAddress): VoiceGeography?
  fun getBarPosition(barNum: Int, end: Boolean = false): BarPosition?
  fun getStartBars(): Int
  fun getEndBars(): Int
  fun getStartBar(): Int
  fun getEndBar(): Int
  fun getOffsetLookup(): OffsetLookup
  fun getScoreQuery(): ScoreQuery
  fun getSegmentLookup(): SegmentLookup
  fun replaceSegments(newSegments: SegmentLookup): StavePositionFinder
  val singlePartMode: Boolean
  val staveId: StaveId
}