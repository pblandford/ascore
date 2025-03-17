package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLoge

fun <T : ScoreLevel> T.removeSameLater(
  eventType: EventType, eventAddress: EventAddress,
  isSame: Event.() -> Boolean
): AnyResult<T> {
  val res = eventMap.getEventAfter(eventType, eventAddress)?.let { next ->
    if (next.second.isSame()) {
      replaceSelf(eventMap.deleteEvent(next.first.eventAddress, eventType))
    } else this
  } ?: this
  return Right(res as T)
}

fun Score.transformVoiceMaps(
  start: BarNum = 1, end: BarNum = numBars,
  staves: List<StaveId> = getAllStaves(true),
  action: VoiceMap.(EventAddress) -> VoiceMapResult
): ScoreResult {
  return transformBars(start, end, staves) { _, _, bar, staveId ->
    this.transformVoiceMaps(EventAddress(bar, staveId = staveId), action)
  }
}

fun Score.transformBars(
  start: BarNum = 1, end: BarNum = numBars,
  staves: List<StaveId> = getAllStaves(true),
  action: Bar.(start: Offset?, end: Offset?, barNum: Int, staveId: StaveId) -> BarResult
): ScoreResult {
  return transformStaves(start, end, staves) { _, _, si ->
    this.transformBars(eas(start, dZero(), si), eas(end, dZero(), si), si, action)
  }
}

fun Part.transformBars(
  start: BarNum, end: BarNum,
  part: PartNum,
  action: Bar.(start: Offset?, end: Offset?, barNum: Int, staveId: StaveId) -> BarResult
): PartResult {
  return transformStaves(start, end, part) { s, e, si ->
    this.transformBars(eas(start, dZero(), si), eas(end, dZero(), si), si, action)
  }
}


//fun Score.transformParts(
//  start: BarNum = 1,
//  end: BarNum = numBars,
//  partIds:List<Int> = subLevels.indices.toList().map { it + 1 },
//  action: Part.(BarNum, BarNum) -> PartResult
//): ScoreResult {
//  val parts = subLevels.withIndex()
//    .mapOrFail { (idx, part) ->
//      if (idx + 1 in partIds) {
//        part.action(start, end)
//      } else {
//        part.ok()
//      }
//    }
//  return parts.then {
//    if (it == this.subLevels) this.ok()
//    else Right(replaceSelf(eventMap, it))
//  }
//}


fun Score.transformStaves(
  start: BarNum = 1,
  end: BarNum = numBars,
  staves: List<StaveId> = getAllStaves(true),
  action: Stave.(BarNum, BarNum, StaveId) -> StaveResult
): ScoreResult {
  val partResult = subLevels.withIndex()
    .mapOrFail { (idx, part) ->
      if (staves.any { it.main == idx + 1 })
        part.transformStaves(
        start, end, idx + 1,
        part.staves.indices.filter { StaveId(idx + 1, it + 1) in staves }, action
      )
      else
        part.ok()
    }
  return partResult.then { parts ->
    if (parts == this.subLevels) this.ok()
    else Right(Score(parts, eventMap, beamDirectory, oLookup))
  }
}

fun Part.transformStaves(
  start: BarNum,
  end: BarNum,
  part: PartNum,
  staveIndices: List<Int> = (staves.indices).toList(),
  action: Stave.(BarNum, BarNum, StaveId) -> StaveResult
): PartResult {
  val transformed = staves.withIndex().mapOrFail { (idx, stave) ->
    if (idx in staveIndices) stave.action(start, end, StaveId(part, idx + 1))
    else stave.ok()
  }
  return transformed.then { Right(Part(it, eventMap)) }
}

fun Stave.transformBars(
  startInclusive: EventAddress, endExclusive: EventAddress,
  staveId: StaveId,
  action: Bar.(start: Offset?, end: Offset?, barNum: Int, staveId: StaveId) -> BarResult
): StaveResult {
  val startBar =
    if (startInclusive.barNum.isWild() || startInclusive.barNum < 1) 1 else startInclusive.barNum
  val endBar =
    if (endExclusive.barNum.isWild() || endExclusive.barNum > numBars) numBars else endExclusive.barNum
  val bars =
    (startBar..endBar).mapNotNull { num -> bars[num - 1]?.let { num to it } }

  val transformed = bars.mapOrFail { (num, bar) ->
    val startOffset = if (num == startBar) startInclusive.offset else null
    val endOffset = if (num == endBar) endExclusive.offset else null
    bar.action(startOffset, endOffset, num, staveId).then { Right(num to it) }
  }


  return transformed.then { list ->
    val allBars = this.bars.take(startBar - 1).plus(list.map { it.second })
      .plus(this.bars.takeLast(numBars - endBar))
    val res = Stave(allBars, eventMap)
    Right(res)
  }
}

fun Bar.transformVoiceMaps(
  eventAddress: EventAddress,
  action: VoiceMap.(EventAddress) -> VoiceMapResult
): BarResult {

  val transformed = voiceMaps.withIndex().mapOrFail { (idx, voiceMap) ->
    voiceMap.action(eventAddress.copy(voice = idx + 1)).then { Right(idx + 1 to it) }
  }
  return transformed.then {
    val res = it.fold(this) { bar, (voiceNum, voice) ->
      bar.replaceSubLevel(voice, voiceNum) as Bar
    }
    Right(res)
  }
}

fun VoiceMap.transformDurationEvents(
  startInclusive: Offset?, endExclusive: Offset?,
  action: (EventMapKey, Event) -> Event
): VoiceMapResult {
  var events = getEvents(EventType.DURATION)
  events = events?.map { (key, event) ->
    if (key.inRange(startInclusive, endExclusive)) {
      key to action(key, event)
    } else key to event
  }?.toMap() ?: eventHashOf()
  return Right(replaceVoiceEvents(events))
}

private fun EventMapKey.inRange(start: Offset?, endExclusive: Offset?): Boolean {
  return eventAddress.offset >= (start ?: dZero()) && eventAddress.offset < (endExclusive ?: dMax())
}

fun Score.divideDuration(
  duration: Duration,
  eventAddress: EventAddress
): AnyResult<List<Pair<EventAddress, Duration>>> {
  var splitState = SplitState(eventAddress, duration, listOf())
  while (splitState.remainder > dZero()) {
    when (val res = getSplit(splitState)) {
      is Right -> splitState = res.r
      is Left -> return Left(res.l)
      else -> {}
    }
  }
  return Right(splitState.bits)
}

private data class SplitState(
  val eventAddress: EventAddress, val remainder: Duration,
  val bits: List<Pair<EventAddress, Duration>>
)

private fun Score.getSplit(splitState: SplitState): AnyResult<SplitState> {
  return splitOverTuplet(splitState).otherwise {
    splitNormal(splitState)
  }
}

private fun Score.splitNormal(splitState: SplitState): AnyResult<SplitState> {
  val timeSignature = getTimeSignature(splitState.eventAddress)
    ?: return asError("No time signature at ${splitState.eventAddress}")
  val left = timeSignature.duration - splitState.eventAddress.offset
  return splitState.resolve(left, splitState.eventAddress.startBar().inc())
}

private fun SplitState.resolve(left: Duration, nextAddress: EventAddress): AnyResult<SplitState> {
  return if (left < remainder) {
    if (left <= dZero()) {
      asError("Left is 0!")
    } else {
      Right(SplitState(nextAddress, remainder - left, bits.plus(eventAddress to left)))
    }
  } else {
    if (remainder == dZero()) {
      asError("Remainder is 0!")
    } else {
      Right(copy(remainder = dZero(), bits = bits.plus(eventAddress to remainder)))
    }
  }
}

private fun Score.splitOverTuplet(splitState: SplitState): AnyResult<SplitState> {
  return getEventAt(EventType.TUPLET, splitState.eventAddress)?.let { (key, tupletEvent) ->
    tuplet(tupletEvent)?.let { tuplet ->
      val left = key.eventAddress.offset + tuplet.realDuration - splitState.eventAddress.offset
      val leftWritten = tuplet.realToWritten(left)
      val endTuplet =
        addDuration(
          key.eventAddress,
          tuplet.realDuration
        )?.copy(staveId = splitState.eventAddress.staveId, voice = splitState.eventAddress.voice)
          ?: splitState.eventAddress.inc().startBar()
      splitState.resolve(leftWritten, endTuplet)
    }
  } ?: AbortNoError("")
}

internal fun VoiceMap.transformEvents(
  start: Offset?,
  end: Offset?,
  action: Event.() -> Event
): VoiceMap {
  val startAddress = start?.let { ez(0, it) }
  val endAddress = end?.let { ez(0, it) }
  var events = eventMap.getEvents(EventType.DURATION, startAddress, endAddress) ?: eventHashOf()
  events = events.map { (k, v) -> k to v.action() }.toMap()
  val em = events.toList().fold(eventMap) { e, (k, v) -> e.putEvent(k.eventAddress, v) }
  val tuplets = tuplets.map { tuplet ->
    tuplet.transformEvents(
      startAddress?.let { tuplet.stripAddress(it, EventType.DURATION) }?.offset,
      endAddress?.let { tuplet.stripAddress(it, EventType.DURATION) }?.offset, action
    )
  }

  return replaceSelf(em, tuplets) as VoiceMap
}