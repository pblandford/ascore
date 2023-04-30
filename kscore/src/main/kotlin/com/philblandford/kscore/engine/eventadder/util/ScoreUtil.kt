package com.philblandford.kscore.engine.eventadder.util

import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.eventadder.subadders.transformBars
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*

internal fun Score.getLevel(
  eventDestination: EventDestination,
  eventAddress: EventAddress,
  eventType: EventType = EventType.NO_TYPE
): ScoreLevel? {
  eventDestination.levels.forEach { type ->
    when (type) {
      ScoreLevelType.SCORE -> this
      ScoreLevelType.PART -> getPart(eventAddress.staveId.main)
      ScoreLevelType.STAVE -> getStave(eventAddress.staveId)
      ScoreLevelType.BAR -> getBar(eventAddress, eventType)
      ScoreLevelType.VOICEMAP -> getVoiceMap(eventAddress)
      else -> null
    }?.let { return it }
  }
  return null
}

internal fun Score.getAllLevels(scoreLevelType: ScoreLevelType): Map<EventAddress, ScoreLevel> {
  return when (scoreLevelType) {
    ScoreLevelType.SCORE -> mapOf(eZero() to this)
    ScoreLevelType.PART -> getAllParts()
    ScoreLevelType.STAVE -> getAllStaves()
    ScoreLevelType.BAR -> getAllBars()
    ScoreLevelType.VOICEMAP -> getAllVoiceMaps()
    ScoreLevelType.NONE -> mapOf()
  }
}

private fun Score.getAllParts(): Map<EventAddress, Part> {
  return parts.withIndex()
    .map { ivPart -> EventAddress(staveId = StaveId(ivPart.index + 1, 0)) to ivPart.value }.toMap()
}

private fun Score.getAllStaves(): Map<EventAddress, Stave> {
  return getAllParts().flatMap { (partAddress, part) ->
    part.staves.withIndex().map { ivStave ->
      EventAddress(
        staveId = StaveId(
          partAddress.staveId.main,
          ivStave.index + 1
        )
      ) to ivStave.value
    }
  }.toMap()
}

private fun Score.getAllBars(): Map<EventAddress, Bar> {
  return getAllStaves().flatMap { (staveAddress, stave) ->
    stave.bars.withIndex().map { ivBar ->
      staveAddress.copy(barNum = ivBar.index + 1) to ivBar.value
    }
  }.toMap()
}

private fun Score.getAllVoiceMaps(): Map<EventAddress, VoiceMap> {
  return getAllBars().flatMap { (barAddress, bar) ->
    bar.voiceMaps.withIndex().map { ivVoiceMap ->
      barAddress.copy(voice = ivVoiceMap.index + 1) to ivVoiceMap.value
    }
  }.toMap()
}

internal fun Score.getOrCreateVoiceMap(
  eventAddress: EventAddress
): VoiceMap {
  return getVoiceMap(eventAddress) ?: run {
    val timeSignature = getTimeSignature(eventAddress) ?: TimeSignature(4, 4)
    val vm = VoiceMap(timeSignature)
    changeSubLevel(vm, eventAddress)
    vm
  }
}

internal fun Score.addEventToLevel(
  eventAddress: EventAddress, scoreLevelType: ScoreLevelType,
  event: Event
): ScoreResult {
  return getSubLevel(eventAddress, scoreLevelType)?.let { sl ->
    val newLevel = sl.replaceSelf(sl.eventMap.putEvent(eventAddress.strip(scoreLevelType), event))
    changeSubLevel(newLevel, eventAddress)
  } ?: Left(
    NotFound(
      "Level $scoreLevelType not found at $eventAddress"
    )
  )
}

internal fun Score.changeSubLevel(
  subLevel: ScoreLevel, eventAddress: EventAddress
): ScoreResult {
  val result: ScoreResult? = when (subLevel.scoreLevelType) {
    ScoreLevelType.SCORE -> Right(
      subLevel as Score
    )
    ScoreLevelType.PART -> Right(
      replaceSubLevel(subLevel, eventAddress.staveId.main) as Score
    )
    ScoreLevelType.STAVE -> {
      getPart(eventAddress.staveId.main)?.replaceSubLevel(subLevel, eventAddress.staveId.sub)
        ?.let { part ->
          changeSubLevel(part, eventAddress)
        }
    }
    ScoreLevelType.BAR -> {
      getStave(eventAddress.staveId)?.replaceSubLevel(subLevel, eventAddress.barNum)
        ?.let { stave ->
          changeSubLevel(stave, eventAddress)
        }
    }
    ScoreLevelType.VOICEMAP -> {
      getBar(eventAddress)?.replaceSubLevel(subLevel, eventAddress.voice)
        ?.let { bar ->
          changeSubLevel(bar, eventAddress)
        }
    }
    else -> null
  }
  return result ?: Left(
    Error("Could not replace sublevel $subLevel")
  )
}

internal fun EventAddress.strip(
  scoreLevelType: ScoreLevelType,
  eventType: EventType = EventType.NO_TYPE
): EventAddress {
  return when (scoreLevelType) {
    ScoreLevelType.SCORE -> scoreStrip(eventType)
    ScoreLevelType.PART -> staveless()
    ScoreLevelType.STAVE -> stavelessWithId()
    ScoreLevelType.BAR -> eZero().copy(offset = offset, graceOffset = graceOffset, voice = 0)
    ScoreLevelType.VOICEMAP -> eZero().copy(offset = offset, graceOffset = graceOffset, id = id)
    else -> this
  }
}

private fun EventAddress.scoreStrip(eventType: EventType): EventAddress =
  when (eventType) {
    EventType.STAVE_JOIN -> copy(
      barNum = 0,
      offset = dZero(),
      graceOffset = null
    )
    EventType.FERMATA -> staveless()
    else -> staveless().startBar()
  }


internal fun EventAddress.badge(
  scoreLevelType: ScoreLevelType,
  levelAddress: EventAddress
): EventAddress {
  return when (scoreLevelType) {
    ScoreLevelType.SCORE -> this
    ScoreLevelType.PART -> copy(staveId = levelAddress.staveId)
    ScoreLevelType.STAVE -> copy(staveId = levelAddress.staveId)
    ScoreLevelType.BAR -> copy(barNum = levelAddress.barNum, staveId = levelAddress.staveId)
    ScoreLevelType.VOICEMAP -> copy(
      barNum = levelAddress.barNum,
      staveId = levelAddress.staveId,
      voice = levelAddress.voice
    )
    else -> this
  }
}

fun Score.rangeToEventEnd(
  offsetLength: Duration,
  eventAddress: EventAddress,
  endAddress: EventAddress,
  action: Score.(EventAddress) -> ScoreResult
): ScoreResult {
  return getEventEnd(endAddress)?.let { endSegment ->
    addressToOffset(eventAddress)?.let { startOffset ->
      addressToOffset(endSegment)?.let { endOffset ->
        val numOffsets = (endOffset - startOffset).divide(offsetLength).toInt()
        (0 until numOffsets).fold(
          Right(
            this
          ) as ScoreResult
        ) { s, offsetNum ->
          s.then {
            offsetToAddress(startOffset + (offsetLength * offsetNum))?.let { addr ->
              it.action(eventAddress.copy(barNum = addr.barNum, offset = addr.offset))
            } ?: Right(it)
          }
        }
      }
    }
  } ?: Left(
    Error(
      "Failed action over range"
    )
  )
}

fun <T : ScoreLevel> T.addEventToMap(
  eventType: EventType,
  params: ParamMap,
  eventAddress: EventAddress
): T {
  val map = eventMap.putEvent(eventAddress, Event(eventType, params))
  return replaceSelf(map) as T
}


fun <T : ScoreLevel> T.deleteEventFromMap(eventType: EventType, eventAddress: EventAddress): T {
  val map = eventMap.deleteEvent(eventAddress, eventType)
  return replaceSelf(map) as T
}

fun Score.nextEvent(eventType: EventType, eventAddress: EventAddress): Pair<EventAddress, Event>? {
  return getEvents(eventType, eventAddress, eventAddress.copy(barNum = numBars))?.toList()?.drop(1)
    ?.firstOrNull()?.let { it.first.eventAddress to it.second }
}

fun Score.previousEvent(
  eventType: EventType,
  eventAddress: EventAddress
): Pair<EventAddress, Event>? {
  return getEvents(eventType, eventAddress.copy(barNum = 1), eventAddress)?.toList()
    ?.lastOrNull()?.let { it.first.eventAddress to it.second }
}


fun Score.setAccidentals(start: EventAddress? = null, end: EventAddress? = null): ScoreResult {
  val startBar = start?.barNum ?: 1
  val endBar = end?.barNum ?: numBars
  return transformBars(startBar, endBar) { _, _, barNum, staveId ->
    setAccidentals(this@setAccidentals, eas(barNum, dZero(), staveId))
  }
}