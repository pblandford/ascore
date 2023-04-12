package com.philblandford.kscore.engine.map

import com.philblandford.kscore.engine.core.score.EventChangeReturn
import com.philblandford.kscore.engine.core.score.ScoreLevel
import com.philblandford.kscore.engine.duration.dWild
import com.philblandford.kscore.engine.types.*

interface EventAdder {
  fun addEvent(
    happening: Happening, scoreLevel: ScoreLevel, scoreQuery: ScoreQuery?
  ): EventChangeReturn?

  fun deleteEvent(
    happening: Happening, scoreLevel: ScoreLevel, scoreQuery: ScoreQuery?
  ): EventChangeReturn?
}

interface EventPostAdder {
  fun eventPostAdd(
    happening: Happening,
    scoreLevel: ScoreLevel,
    scoreQuery: ScoreQuery?
  ): ScoreLevel
}

data class EventMapKey(val eventType: EventType, val eventAddress: EventAddress) :
  Comparable<EventMapKey> {
  override fun compareTo(other: EventMapKey): Int {
    var ret = eventAddress.compareTo(other.eventAddress)
    if (ret == 0) ret = eventType.ordinal - other.eventType.ordinal
    return ret
  }
}
typealias EMK = EventMapKey

fun DK(eventAddress: EventAddress) =
  EMK(EventType.DURATION, eventAddress)
typealias EventHash = Map<EventMapKey, Event>
typealias EventHashM = MutableMap<EventMapKey, Event>
typealias EventHashSimple = Map<EventType, Event>
typealias EventList = List<Pair<EventMapKey, Event>>

fun eventHashOf(vararg event: Pair<EventMapKey, Event>): Map<EventMapKey, Event> = hashMapOf(*event)
fun eventHashOfM(vararg event: Pair<EventMapKey, Event>): MutableMap<EventMapKey, Event> =
  mutableMapOf(*event)

typealias MapOfMaps = Map<EventType, EventHash>

open class EventMapBasic(private val mapOfMaps: MapOfMaps) : EventMap {

  override fun getEventTypes(): Iterable<EventType> {
    return mapOfMaps.keys
  }

  override fun getAllEvents(options:List<EventGetterOption>): EventHash {
    return mapOfMaps.flatMap { it.value.toList() }.toMap().toSortedMap()
  }

  override fun getEvents(
    eventType: EventType,
    eventAddress: EventAddress?,
    endAddress: EventAddress?,
    options:List<EventGetterOption>
  ): EventHash? {
    eventAddress?.let { start ->
      val end = endAddress ?: eWild()
      return mapOfMaps[eventType]?.filter {
        (start.isWild() || it.key.eventAddress.gte(start)) &&
            (end.isWild() || (end.barNum == it.key.eventAddress.barNum && end.offset.isWild())
                || it.key.eventAddress.lte(end))
      }
    }
    return mapOfMaps[eventType]
  }

  override fun collateEvents(
    eventTypes: Iterable<EventType>,
    eventAddress: EventAddress?,
    endAddress: EventAddress?
  ): EventHash? {
    return eventTypes.toList().fold(eventHashOf()) { hash, type ->
      getEvents(type, eventAddress, endAddress)?.let {
        hash.plus(it)
      } ?: hash
    }
  }

  override fun getEvent(eventType: EventType, eventAddress: EventAddress): Event? {
    return getEvents(eventType)?.get(
      EventMapKey(
        eventType,
        eventAddress
      )
    )
  }

  override fun putEvent(eventAddress: EventAddress, event: Event): EventMap {
    return mapOp(event.eventType) {
      it.plus(
        EventMapKey(
          event.eventType,
          eventAddress
        ) to event
      ).toSortedMap()
    }
  }

  override fun replaceEvents(eventType: EventType, eventHash: EventHash): EventMap {
    return EventMapBasic(
      mapOfMaps.plus(
        Pair(
          eventType,
          eventHash
        )
      )
    )
  }

  override fun <T> getParam(
    eventType: EventType,
    eventParam: EventParam,
    eventAddress: EventAddress
  ): T? {
    return getEvent(eventType, eventAddress)?.getParam<T>(eventParam)
  }

  override fun <T> getParamAt(
    eventType: EventType,
    eventParam: EventParam,
    eventAddress: EventAddress
  ): T? {
    return getEventAt(eventType, eventAddress)?.second?.getParam<T>(eventParam)
  }

  override fun setParam(
    eventAddress: EventAddress,
    eventType: EventType,
    param: EventParam,
    value: Any?
  ): EventMap {
    val event = getEvent(eventType, eventAddress) ?: Event(eventType, paramMapOf())
    return putEvent(eventAddress, event.addParam(param, value))
  }

  override fun deleteEvent(
    startAddress: EventAddress,
    eventType: EventType,
    endAddress: EventAddress?,
    spareMe: (EventType, EventAddress) -> Boolean
  ): EventMap {
    val eventMap = mapOp(eventType) {
      if (endAddress == null) {
        if (!spareMe(eventType, startAddress)) {
          it.minus(EventMapKey(eventType, startAddress))
        } else {
          it
        }
      } else {
        it.filter {
          !spareMe(it.key.eventType, it.key.eventAddress) &&
              inRange(it.key.eventAddress, startAddress, endAddress)
        }
          .toList()
          .fold(it) { m, ev ->
            m.minus(ev.first)
          }
      }
    }
    return EventMapBasic(eventMap.mapOfMaps.filterNot { it.value.isEmpty() })
  }

  override fun getEventAfter(
    eventType: EventType,
    eventAddress: EventAddress
  ): Pair<EventMapKey, Event>? {
    return mapOfMaps[eventType]?.entries?.sortedBy { it.key.eventAddress }?.dropWhile {
      it.key.eventAddress.barNum < eventAddress.barNum ||
          (it.key.eventAddress.barNum == eventAddress.barNum && it.key.eventAddress.offset <= eventAddress.offset)
    }?.firstOrNull() { it.key.eventAddress.id == eventAddress.id }
      ?.toPair()
  }

  private fun inRange(eventAddress: EventAddress, start: EventAddress, end: EventAddress): Boolean {
    if (eventAddress.barNum == end.barNum && eventAddress.offset >= start.offset && end.offset == dWild()) {
      return true
    }
    return eventAddress in start..end
  }

  override fun deleteAll(eventType: EventType): EventMap {
    return EventMapBasic(mapOfMaps.minus(eventType))
  }

  override fun deleteRange(
    start: EventAddress,
    end: EventAddress,
    spareMe: (EventType, EventAddress) -> Boolean
  ): EventMap {
    return mapOfMaps.keys.fold(this as EventMap) { map, type ->
      map.deleteEvent(start, type, end, spareMe)
    }
  }

  override fun getEventAt(

    eventType: EventType,
    eventAddress: EventAddress
  ): Pair<EventMapKey, Event>? {
    return mapOfMaps[eventType]?.entries?.sortedBy { it.key.eventAddress }
      ?.takeWhile { (key, _) ->
        key.eventAddress.barNum < eventAddress.barNum ||
            (key.eventAddress.barNum == eventAddress.barNum && key.eventAddress.offset <= eventAddress.offset)
      }?.lastOrNull { it.key.eventAddress.id == eventAddress.id }?.let {
        if (it.value.getParam<Boolean>(EventParam.END) == true && it.key.eventAddress.horizontal !=
            eventAddress.horizontal) null else it
      }
      ?.toPair()
  }

  override fun getAllEvents(segment: EventAddress): EventHash {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun shiftEvents(from: Int, by: Int, condition: (EventMapKey) -> Boolean): EventMap {
    val newMap = mapOfMaps.map { map ->
      map.key to map.value.shift(from, by, condition)
    }.toMap()
    return EventMapBasic(newMap)
  }

  private fun EventHash.shift(from: Int, by: Int, condition: (EventMapKey) -> Boolean): EventHash {
    val list = toList()
    val beforeAfter = list.partition { it.first.eventAddress.barNum < from || !condition(it.first) }
    val before = beforeAfter.first
    var after = beforeAfter.second


    val end = if (by < 0) from - by else from
    after = after.dropWhile { it.first.eventAddress.barNum < end }.map { (emk, event) ->
      emk.copy(eventAddress = emk.eventAddress.inc(by)) to event
    }
    return before.plus(after).toMap()
  }


  private fun mapOp(eventType: EventType, op: (EventHash) -> EventHash): EventMapBasic {
    val mom =
      mapOfMaps[eventType]?.let { mapOfMaps } ?: mapOfMaps.plus(
        Pair(
          eventType,
          eventHashOf()
        )
      )
    return mom[eventType]?.let {
      val newSubMap = op(it)
      val newMap = mom.plus(Pair(eventType, newSubMap))
      EventMapBasic(newMap)
    } ?: this
  }
}

fun eventMapOf(eventHash: EventHash): EventMap {
  return eventHash.toList().fold(emptyEventMap()) { map, entry ->
    map.putEvent(entry.first.eventAddress, entry.second)
  }
}

fun emptyEventMap(): EventMap =
  EventMapBasic(mutableMapOf())