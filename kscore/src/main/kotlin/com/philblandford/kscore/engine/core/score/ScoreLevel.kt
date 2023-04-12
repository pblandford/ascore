package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.core.representation.RepUpdate
import com.philblandford.kscore.engine.core.representation.RepUpdateFull
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLoge
import org.apache.commons.math3.util.FastMath.min

data class ScoreReturn(
  val scoreLevel: ScoreLevel, val passUp: Iterable<Happening> = listOf(),
  val passDown: Iterable<Happening> = listOf(),
  val repUpdate: RepUpdate = RepUpdateFull
)

data class EventChangeReturn(
  val newMap: EventMap,
  val newSubLevels: Iterable<ScoreLevel>? = null,
  val passUp: Iterable<Happening> = listOf(),
  val passDown: Iterable<Happening> = listOf(),
  val passSideWays: Iterable<Happening> = listOf(),
  val replacedSublevels: Iterable<Pair<EventAddress, ScoreLevel>> = listOf(),
  val repUpdate: RepUpdate = RepUpdateFull
)

enum class ScoreLevelType {
  SCORE, PART, STAVE, BAR, VOICEMAP, NONE
}

interface ScoreLevel : EventGetter {

  open val eventMap: EventMap

  val subLevels: List<ScoreLevel>

  val scoreLevelType: ScoreLevelType

  val subLevelType: ScoreLevelType

  fun getSubLevel(eventAddress: EventAddress): ScoreLevel?
  fun getSubLevel(eventAddress: EventAddress, type: ScoreLevelType): ScoreLevel? {
    return getSubLevel(eventAddress)?.let { subLevel ->
      if (subLevel.scoreLevelType == type) {
        subLevel
      } else {
        subLevel.getSubLevel(eventAddress, type)
      }
    }
  }

  fun getSubLevels(
    eventAddress: EventAddress?,
    endAddress: EventAddress?
  ): Iterable<Pair<ScoreLevel, Int>> {
    val all = getAllSubLevels()
    return if (eventAddress == null) {
      all.withIndex().map { it.value to it.index + 1 }
    } else {
      if (subLevelIdx(eventAddress).isWild()) {
        all.withIndex().map { it.value to it.index + 1 }
      } else {
        endAddress?.let {
          getAllSubLevels().withIndex().filter { iv ->
            subLevelIdx(eventAddress) <= iv.index + 1 &&
                subLevelIdx(endAddress) >= iv.index + 1
          }.map { it.value to it.index + 1 }
        } ?: listOfNotNull(getSubLevel(eventAddress)?.let { it to subLevelIdx(eventAddress) })

      }
    }
  }

  fun removeSubLevels(start: Int, end: Int): ScoreLevel {
    val removeEnd = min(end + 1, subLevels.count())
    val newLevels = subLevels.minus(subLevels.toList().subList(start, removeEnd))
    return replaceSelf(eventMap, newLevels)
  }

  fun addSubLevels(from: Int, newLevels: List<ScoreLevel>): ScoreLevel {
    val mutable = subLevels.toMutableList()
    mutable.addAll(from - 1, newLevels)
    return replaceSelf(eventMap, mutable.toList())
  }

  fun getAllSubLevels(): Iterable<ScoreLevel>
  fun subLevelIdx(eventAddress: EventAddress): Int
  fun replaceSubLevel(scoreLevel: ScoreLevel, index: Int): ScoreLevel
  fun replaceSelf(eventMap: EventMap, newSubLevels: Iterable<ScoreLevel>? = null): ScoreLevel

  fun getAllLevelEvents(): EventHash {
    return eventMap.getAllEvents()
  }

  override fun getAllEvents(options: List<EventGetterOption>): EventHash {
    val events = getAllLevelEvents().toMutableMap()
    getAllSubLevels().withIndex().toList().forEach { sl ->
      val subEvents = sl.value.getAllEvents().map {
        it.key.copy(eventAddress = badgeEventAddress(it.key.eventAddress, sl.index + 1)) to it.value
      }
      events.putAll(subEvents)
    }
    return events.toMap()
  }

  override fun getEvents(
    eventType: EventType,
    eventAddress: EventAddress?,
    endAddress: EventAddress?,
    options:List<EventGetterOption>
  ): EventHash? {

    val stripped = eventAddress?.let { stripAddress(it, eventType) }
    val prepared = eventAddress?.let { prepareAddress(it, eventType) }
    val preparedEnd = endAddress?.let { prepareAddress(it, eventType) }

    val thisEvents = getSpecialEvents(eventType)
      ?: eventMap.getEvents(eventType, prepared, preparedEnd) ?: mapOf()

    val lowerEvents = if (thisEvents.isEmpty() || continueGathering(eventType)) {
      foldSubLevelEvents(eventType, eventAddress, endAddress)
        ?: stripped?.let { getSubLevel(it)?.getEvents(eventType, it) }
        ?: mapOf()
    } else mapOf()
    return thisEvents.plus(lowerEvents)
  }

  override fun collateEvents(
    eventTypes: Iterable<EventType>,
    eventAddress: EventAddress?,
    endAddress: EventAddress?
  ): EventHash? {
    val events = eventHashOf().toMutableMap()
    eventTypes.toList().forEach { type ->
      getEvents(type, eventAddress, endAddress)?.let { events.putAll(it) }
    }
    return events.toMap()
  }

  override fun getAllEvents(segment: EventAddress): EventHash {
    return doGetAllEvents(segment)
  }

  fun continueGathering(eventType: EventType) = false

  fun doGetAllEvents(
    segment: EventAddress
  ): MutableMap<EventMapKey, Event> {
    val map = mutableMapOf<EventMapKey, Event>()
    val stripped = stripAddress(segment, EventType.NO_TYPE)
    val events = eventMap.getAllEvents().filter { it.key.eventAddress.idless() == stripped }
    map.putAll(events)
    getSubLevel(stripped)?.doGetAllEvents(stripped)?.let { subEvents ->

      val badged = subEvents.map {
        it.key.copy(
          eventAddress = badgeEventAddress(
            it.key.eventAddress,
            subLevelIdx(stripped)
          )
        ) to it.value
      }
      map.putAll(badged)
    }
    return map
  }

  fun badgeEventAddress(eventAddress: EventAddress, levelIdx: Int): EventAddress

  fun foldSubLevelEvents(
    eventType: EventType,
    eventAddress: EventAddress?,
    endAddress: EventAddress?
  ): EventHash? {
    val eventList = mutableListOf<Pair<EventMapKey, Event>>()
    getSubLevels(eventAddress, endAddress).forEach { iv ->
      val events = iv.first.getEvents(eventType, eventAddress, endAddress)?.map {
        Pair(
          it.key.copy(eventAddress = badgeEventAddress(it.key.eventAddress, iv.second)),
          it.value
        )
      }
      events?.let { eventList.addAll(it) }
    }
    return eventList.toMap()
  }

  override fun getEvent(eventType: EventType, eventAddress: EventAddress): Event? {
    val stripped = stripAddress(eventAddress, eventType)
    val prepared = prepareAddress(stripped, eventType)
    return getSpecialEvent(eventType, stripped)
      ?: eventMap.getEvent(eventType, prepared)
      ?: getSubLevel(prepared)?.getEvent(eventType, prepared)
  }

  override fun getEventAt(
    eventType: EventType,
    eventAddress: EventAddress
  ): Pair<EventMapKey, Event>? {
    val stripped = stripAddress(eventAddress, eventType)
    return getSpecialEventAt(eventType, eventAddress)
      ?: eventMap.getEventAt(eventType, stripped)
      ?: getSubLevel(eventAddress)?.getEventAt(eventType, stripped)?.let {
        it.first.copy(
          eventAddress = badgeEventAddress(
            it.first.eventAddress,
            subLevelIdx(eventAddress)
          )
        ) to it.second
      }
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

  fun getSpecialEvent(eventType: EventType, eventAddress: EventAddress): Event? {
    return null
  }


  fun getSpecialEventAt(
    eventType: EventType,
    eventAddress: EventAddress
  ): Pair<EventMapKey, Event>? {
    return null
  }


  fun getSpecialEvents(eventType: EventType): EventHash? {
    return null
  }

  fun stripAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    return eventAddress
  }

  fun prepareAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    return eventAddress
  }

}

var SCORELEVEL_CACHE_ENABLE = true


abstract class ScoreLevelImpl : ScoreLevel {

  private data class CacheKey(
    val eventType: EventType,
    val eventAddress: EventAddress?,
    val endAddress: EventAddress?
  )

  private val cache = mutableMapOf<CacheKey, EventHash>()

  override fun getEvents(
    eventType: EventType,
    eventAddress: EventAddress?,
    endAddress: EventAddress?,
    options: List<EventGetterOption>
  ): EventHash? {
    val key = CacheKey(eventType, eventAddress, endAddress)

    if (!SCORELEVEL_CACHE_ENABLE) {
      ksLoge("Cache has been turned off - this had better be a test..")
    }

    val res = if (SCORELEVEL_CACHE_ENABLE) cache[key] else null
    return res ?: run {
      super.getEvents(eventType, eventAddress, endAddress, options)?.let { res ->
        cache.put(key, res)
        res
      }
    }
  }

  private var allEvents: EventHash? = null

  override fun getAllEvents(options: List<EventGetterOption>): EventHash {
    return allEvents ?: run {
      allEvents = super.getAllEvents(options)
      allEvents
    } ?: eventHashOf()
  }

  private var getEventCache = mutableMapOf<Pair<EventType, EventAddress>, Event>()
  override fun getEvent(eventType: EventType, eventAddress: EventAddress): Event? {
    val key = Pair(eventType, eventAddress)
    return getEventCache[key] ?: run {
      super.getEvent(eventType, eventAddress)?.let { res ->
        getEventCache.put(key, res)
        res
      }
    }
  }


  private var getEventAtCache =
    mutableMapOf<Pair<EventType, EventAddress>, Pair<EventMapKey, Event>>()

  override fun getEventAt(
    eventType: EventType,
    eventAddress: EventAddress
  ): Pair<EventMapKey, Event>? {
    val key = Pair(eventType, eventAddress)
    return getEventAtCache[key] ?: run {
      super.getEventAt(eventType, eventAddress)?.let { res ->
        getEventAtCache.put(key, res)
        res
      }
    }
  }


  private data class CollateKey(
    val eventTypes: Iterable<EventType>, val eventAddress: EventAddress?,
    val endAddress: EventAddress?
  )

  private val collatedEventCache = mutableMapOf<CollateKey, EventHash?>()
  override fun collateEvents(
    eventTypes: Iterable<EventType>,
    eventAddress: EventAddress?,
    endAddress: EventAddress?
  ): EventHash? {
    val key = CollateKey(eventTypes, eventAddress, endAddress)
    return collatedEventCache[key] ?: run {
      val res = super.collateEvents(eventTypes, eventAddress, endAddress)
      collatedEventCache.put(key, res)
      res
    }
  }


  private data class SLCacheKey(val eventAddress: EventAddress?, val endAddress: EventAddress?)

  private val slCache = mutableMapOf<SLCacheKey, Iterable<Pair<ScoreLevel, Int>>>()
  override fun getSubLevels(
    eventAddress: EventAddress?,
    endAddress: EventAddress?
  ): Iterable<Pair<ScoreLevel, Int>> {
    val key = SLCacheKey(eventAddress, endAddress)
    return slCache[key] ?: run {
      val res = super.getSubLevels(eventAddress, endAddress)
      slCache.put(key, res)
      res
    }
  }


}