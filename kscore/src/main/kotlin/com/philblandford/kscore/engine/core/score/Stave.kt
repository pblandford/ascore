package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.emptyEventMap
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.util.replace
import java.util.*

private val defaultEventMap = {
  emptyEventMap().putEvent(
      ez(1),
      Event(EventType.CLEF, paramMapOf(EventParam.TYPE to ClefType.TREBLE))
  )
}

data class Stave(
    val bars: List<Bar> = listOf(Bar()),
    override val eventMap: EventMap = defaultEventMap()
) : ScoreLevelImpl() {

  override val subLevels = bars
  val numBars = subLevels.size

  val segments = TreeSet(bars.withIndex().flatMap { (idx, bar) ->
    bar.segments.map { it.copy(barNum = idx + 1) }
  })

  fun getPreviousStaveSegment(eventAddress: EventAddress): EventAddress? {
    return segments.lower(eventAddress.staveless())
  }

  override fun getSubLevel(eventAddress: EventAddress): ScoreLevel? {
    return bars.getOrNull(eventAddress.barNum - 1)
  }

  override fun getAllSubLevels(): Iterable<ScoreLevel> {
    return bars
  }

  override fun subLevelIdx(eventAddress: EventAddress): Int {
    return eventAddress.barNum
  }

  override fun badgeEventAddress(eventAddress: EventAddress, levelIdx: Int): EventAddress {
    return EventAddress(
        levelIdx, eventAddress.offset, eventAddress.graceOffset,
        eventAddress.staveId, eventAddress.voice, eventAddress.id
    )
  }

  override fun replaceSubLevel(scoreLevel: ScoreLevel, index: Int): ScoreLevel {
    return Stave(
        bars.replace(index - 1, scoreLevel as Bar),
        eventMap
    )
  }

  override fun replaceSelf(eventMap: EventMap, newSubLevels: Iterable<ScoreLevel>?): ScoreLevel {
    return Stave(newSubLevels?.map { it as Bar }?.toList() ?: bars, eventMap)
  }

  override fun stripAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    val stripped = eventAddress.copy(staveId = sZero())
    return if (eventType == EventType.DURATION) {
      val bar = getBarNum(eventAddress.barNum, eventType)
      if (bar != eventAddress.barNum) stripped.copy(barNum = bar) else stripped
    } else stripped
  }

  override fun prepareAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    return when (eventType) {
      EventType.SLUR, EventType.OCTAVE, EventType.EXPRESSION_DASH, EventType.LONG_TRILL,
      EventType.GLISSANDO, EventType.DYNAMIC, EventType.CLEF, EventType.PEDAL -> eventAddress.stavelessWithId()
      EventType.DURATION -> eventAddress.copy(barNum = getBarNum(eventAddress.barNum, eventType))
      EventType.REPEAT_BAR -> eventAddress.startBar().staveless()
      EventType.INSTRUMENT -> eventAddress.staveless().voiceIdless()
      else -> eventAddress
    }
  }

  override val scoreLevelType = ScoreLevelType.STAVE
  override val subLevelType = ScoreLevelType.BAR

  override fun foldSubLevelEvents(
      eventType: EventType,
      eventAddress: EventAddress?,
      endAddress: EventAddress?
  ): EventHash? {
    val eventList = mutableListOf<Pair<EventMapKey, Event>>()
    getSubLevels(eventAddress, endAddress).forEach { iv ->
      val actualBar = getBar(iv.second, eventType) ?: iv.first
      val events = actualBar.getEvents(eventType, eventAddress, endAddress)?.map {
        Pair(
            it.key.copy(eventAddress = badgeEventAddress(it.key.eventAddress, iv.second)),
            it.value
        )
      }
      events?.let { eventList.addAll(it) }
    }
    return eventList.toMap()
  }

  fun getBar(num: Int, eventType: EventType = EventType.NO_TYPE): Bar? {
    return bars.getOrNull(getBarNum(num, eventType) - 1)
  }

  private val neverPrevious = setOf(EventType.BEAM, EventType.HARMONY)

  private fun getBarNum(barNum: Int, eventType: EventType): Int {
    if (neverPrevious.contains(eventType)) {
      return barNum
    }

    eventMap.getEvent(EventType.REPEAT_BAR, ez(barNum - 1))?.let { repeat ->
      if (repeat.getParam<Int>(EventParam.NUMBER) == 2 && barNum > 2) {
        return getBarNum(barNum - 2, eventType)
      }
    }

    return eventMap.getEvent(EventType.REPEAT_BAR, ez(barNum))?.let { repeat ->
      if (barNum > 1) {
        val num = repeat.getParam<Int>(EventParam.NUMBER) ?: 1
        getBarNum(barNum - num, eventType)
      } else barNum
    } ?: barNum

  }

  companion object {
    private fun create(
        numBars: Int,
        ts: TimeSignature = TimeSignature(4, 4),
        upbeat: TimeSignature? = null
    ): Stave {
      val bars = Bar.createBars(numBars, ts, upbeat)
      return Stave(bars.toList())
    }

    fun createStaves(
        num: Int, numBars: Int, ts
        : TimeSignature = TimeSignature(4, 4)
    ): List<Stave> {
      return (1..num).map { create(numBars, ts) }.toList()
    }
  }
}

fun stave(
    clef: ClefType, numBars: Int,
    timeSignature: TimeSignature = TimeSignature(4, 4)): Stave {
  val em =
      emptyEventMap()
          .putEvent(ez(1), Event(EventType.CLEF, paramMapOf(EventParam.TYPE to clef)))
  val bars = (1..numBars).map {
    Bar(timeSignature)
  }
  return Stave(bars, em)
}