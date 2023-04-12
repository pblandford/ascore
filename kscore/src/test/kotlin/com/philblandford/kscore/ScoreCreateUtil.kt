package com.philblandford.kscore

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.beam.BeamMap
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.time.TimeSignature


open class MockScoreQuery(
  override val numBars: Int, override val numParts: Int,
  vararg events: Pair<EventAddress, Event>
) : MockEventGetter(*events), ScoreQuery {

  override fun haveStaveSegment(eventAddress: EventAddress): Boolean {
    return false
  }

  override fun getComposer(): String? {
    TODO("Not yet implemented")
  }

  override fun getOctaveShift(eventAddress: EventAddress): Int {
    TODO("Not yet implemented")
  }

  override fun getSubtitle(): String? {
    TODO("Not yet implemented")
  }

  override fun getPreviousVoiceSegment(eventAddress: EventAddress): EventAddress? {
    TODO("Not yet implemented")
  }

  override fun isRepeatBar(eventAddress: EventAddress): Boolean {
    TODO("Not yet implemented")
  }

  override fun getTitle(): String {
    TODO("Not yet implemented")
  }

  override fun <T> getOption(option: EventParam): T? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getBeams(start: EventAddress?, end: EventAddress?): BeamMap {
    return mapOf()
  }

  override fun getRepeatBars(): Lookup<RepeatBarType> {
    return mapOf()
  }

  override fun numStaves(part: Int): Int {
    return 1
  }

  override fun selectedPart(): Int {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun selectedPartName(): String? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getInstrument(eventAddress: EventAddress, adjustTranspose:Boolean): Instrument? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun isEmptyBar(eventAddress: EventAddress): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getKeySignature(eventAddress: EventAddress, concert:Boolean): Int? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getEventEnd(eventAddress: EventAddress): EventAddress? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getNoteDuration(eventAddress: EventAddress): Duration? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getAllStaves(selected: Boolean): Iterable<StaveId> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getStaveRange(start: StaveId, end: StaveId): Iterable<StaveId> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun numVoicesAt(eventAddress: EventAddress): Int {
    return 1
  }

  override fun getLastSegmentInDuration(address: EventAddress): EventAddress? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getFilename(): String? {
    TODO("Not yet implemented")
  }

  override fun getAllEvents(options: List<EventGetterOption>): EventHash {
    TODO("Not yet implemented")
  }

  override fun getEvents(
    eventType: EventType,
    eventAddress: EventAddress?,
    endAddress: EventAddress?,
    options: List<EventGetterOption>
  ): EventHash? {
    TODO("Not yet implemented")
  }

  override fun getEventsForPart(id: Int, level:Boolean): EventHash {
    return eventHashOf()
  }

  override fun getEventsForStave(
    staveId: StaveId,
    types: Iterable<EventType>,
    start: EventAddress?,
    end: EventAddress?
  ): EventHash {
    return eventHashOf()
  }

  override fun getEventsForBar(eventType: EventType, eventAddress: EventAddress): EventHash {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getTimeSignature(eventAddress: EventAddress): TimeSignature? {
    return TimeSignature(4,4)
  }

  override fun getSystemEvents(): EventHash {
    return eventHashOf()
  }

  override fun singlePartMode(): Boolean {
    return false
  }

  override fun addDuration(address: EventAddress, duration: Duration): EventAddress? {
    return address
  }

  override fun subtractDuration(address: EventAddress, duration: Duration): EventAddress? {
    return address
  }

  override fun offsetToAddress(offset: Duration): EventAddress? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDuration(from: EventAddress, to: EventAddress): Duration? {
    return dZero()
  }

  override fun addressToOffset(address: EventAddress): Duration? {
    return dZero()
  }

  override fun getPreviousStaveSegment(eventAddress: EventAddress): EventAddress? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getFloorStaveSegment(eventAddress: EventAddress): EventAddress? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getNextStaveSegment(eventAddress: EventAddress): EventAddress? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun allParts(selected: Boolean): Iterable<Int> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun allBarAddresses(selected: Boolean): Iterable<EventAddress> {
    return listOf()
  }

  override fun <T> getParam(eventType: EventType, eventParam: EventParam, eventAddress: EventAddress): T? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun <T> getParamAt(eventType: EventType, eventParam: EventParam, eventAddress: EventAddress): T? {
    return null
  }

  override fun getAllEvents(segment: EventAddress): EventHash {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getEmptyVoiceMaps(start: EventAddress?, end: EventAddress?): Iterable<EventAddress> {
    return listOf()
  }

  override fun getNextVoiceSegment(eventAddress: EventAddress): EventAddress? {
    TODO("Not yet implemented")
  }
}

fun msq(numBars: Int = 1, numParts: Int = 1): MockScoreQuery {
  return MockScoreQuery(numBars, numParts)
}


open class MockEventGetter(vararg val events: Pair<EventAddress, Event>) : EventGetter {
//  override fun getAllEvents(): EventHash {
//    return events.map { EventMapKey(
//      it.second.eventType,
//      it.first
//    ) to it.second }.toMap()
//  }
//
//  override fun getEvents(eventType: EventType, eventAddress: EventAddress?, end:EventAddress?): EventHash? {
//    return events.filter { ev -> eventAddress?.let { ev.first.match(it) } ?: true && ev.second.eventType == eventType }
//      .map { EventMapKey(
//        it.second.eventType,
//        it.first
//      ) to it.second }.toMap()
//  }

  override fun getAllEvents(options: List<EventGetterOption>): EventHash {
    TODO("Not yet implemented")
  }

  override fun getEvents(
    eventType: EventType,
    eventAddress: EventAddress?,
    endAddress: EventAddress?,
    options: List<EventGetterOption>
  ): EventHash? {
    TODO("Not yet implemented")
  }

  override fun collateEvents(
    eventTypes: Iterable<EventType>,
    eventAddress: EventAddress?,
    endAddress: EventAddress?
  ): EventHash? {
    return eventHashOf()
  }

  override fun getEvent(eventType: EventType, eventAddress: EventAddress): Event? {
    return getEvents(eventType, eventAddress)?.toList()?.firstOrNull()?.second
  }

  override fun getEventAt(eventType: EventType, eventAddress: EventAddress): Pair<EventMapKey, Event>? {
    return getEvents(eventType, eventAddress)?.toList()?.firstOrNull()
  }

  override fun getAllEvents(segment: EventAddress): EventHash {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun <T> getParam(eventType: EventType, eventParam: EventParam, eventAddress: EventAddress): T? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun <T> getParamAt(eventType: EventType, eventParam: EventParam, eventAddress: EventAddress): T? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}