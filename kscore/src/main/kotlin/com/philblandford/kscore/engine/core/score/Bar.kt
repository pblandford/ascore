package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.emptyEventMap
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.util.replace

data class Bar(
  val timeSignature: TimeSignature = TimeSignature(4, 4),
  val voiceMaps: List<VoiceMap> = listOf(voiceMap(timeSignature)),
  override val eventMap: EventMap = emptyEventMap()
) : ScoreLevelImpl() {

  override val subLevels = voiceMaps

  val voiceNumberMap = voiceNumberMap(voiceMaps)

  val segments = run {
    var segs = voiceMaps.flatMap { vm ->
      vm.getEvents(EventType.DURATION)?.map { it.key.eventAddress.voiceIdless() } ?: listOf()
    }.distinct().sortedBy { it.offset }
    eventMap.getEvents(EventType.PLACE_HOLDER)?.let {
      segs = segs.plus(it.keys.map { it.eventAddress })
    }
    if (segs.isEmpty()) listOf(eZero()) else segs
  }

  val allVoiceEvents = run {
    val evs = voiceMaps.withIndex().flatMap { iv ->
      iv.value.getAllEvents().map {
        it.key.copy(eventAddress = it.key.eventAddress.copy(voice = iv.index + 1)) to it.value
      }
    }
    evs.toMap()
  }

  override fun getSubLevel(eventAddress: EventAddress): ScoreLevel? {
    return voiceMaps.getOrNull(getVoice(eventAddress) - 1)
  }

  override fun getAllSubLevels(): Iterable<ScoreLevel> {
    return voiceMaps
  }

  override fun subLevelIdx(eventAddress: EventAddress): Int {
    return getVoice(eventAddress)
  }

  override fun replaceSubLevel(scoreLevel: ScoreLevel, index: Int): ScoreLevel {
    return Bar(timeSignature, voiceMaps.replace(index - 1, scoreLevel as VoiceMap), eventMap)
  }

  override fun replaceSelf(eventMap: EventMap, newSubLevels: List<ScoreLevel>?): ScoreLevel {
    return Bar(timeSignature, newSubLevels?.map { it as VoiceMap } ?: voiceMaps, eventMap)
  }

  override fun getEvents(
    eventType: EventType,
    eventAddress: EventAddress?,
    endAddress: EventAddress?,
    options:List<EventGetterOption>
  ): EventHash? {
    var events = super.getEvents(eventType, null, null, options)
    eventAddress?.barless()?.let { start ->
      val end = endAddress?.barless() ?: start
      events = events?.filter { (emk, _) ->
        (endAddress == null || (eventType == EventType.BEAM && endAddress.offset.isWild())
            || emk.eventAddress.barless() in start..end)
      }
    }
    return events
  }

  override fun badgeEventAddress(eventAddress: EventAddress, levelIdx: Int): EventAddress {
    return eventAddress.copy(voice = levelIdx)
  }

  override val scoreLevelType = ScoreLevelType.BAR
  override val subLevelType = ScoreLevelType.VOICEMAP

  fun getMap(voice: Int): VoiceMap? {
    return voiceMaps.getOrNull(voice - 1)
  }

  override fun stripAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    return when (eventType) {
      EventType.CLEF, EventType.GLISSANDO, EventType.PAUSE, EventType.SPACE, EventType.HARMONY,
      EventType.PLACE_HOLDER ->
        eZero().copy(offset = eventAddress.offset, voice = 0)
      EventType.DURATION -> eZero().copy(
        offset = eventAddress.offset, graceOffset = eventAddress.graceOffset,
        voice = getVoice(eventAddress)
      )
      EventType.NOTE -> eZero().copy(
        offset = eventAddress.offset, graceOffset = eventAddress.graceOffset,
        voice = getVoice(eventAddress), id = eventAddress.id
      )
      else -> eventAddress
    }
  }

  override fun prepareAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    return when (eventType) {
      EventType.HARMONY -> eventAddress.voiceIdless()
      else -> eventAddress
    }
  }

  private fun getVoice(eventAddress: EventAddress): Int {
    return if (eventAddress.voice == 0) 1 else eventAddress.voice
  }

  fun timeSignature(): TimeSignature? {
    return voiceMaps.firstOrNull()?.timeSignature
  }

  companion object {
    fun createBars(
      num: Int,
      ts: TimeSignature = TimeSignature(4, 4),
      upbeat: TimeSignature? = null
    ): Iterable<Bar> {
      return (1..num).map {
        val time = if (it == 1) {
          upbeat ?: ts
        } else ts
        Bar(ts, listOf(voiceMap(time)))
      }
    }
  }
}
