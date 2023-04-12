package com.philblandford.kscore.engine.core.areadirectory.header

import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.pitch.KeySignature
import com.philblandford.kscore.engine.pitch.transposeKey
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.option.getOption

/* A map for each header area, showing which events it needs to render */
internal typealias HeaderEventMap = Map<EventType, Event>

private fun headerEventMapOf() = mutableMapOf<EventType, Event>()

/* Get event maps for every *potential* header area - we don't yet know which bars will be
  at the start of the stave, so we assume every bar could be
 */
internal fun createHeaderEventMaps(scoreQuery: ScoreQuery): Lookup<HeaderEventMap> {
  val map = mutableMapOf<EventAddress, HeaderEventMap>()
  val clefs = scoreQuery.getEvents(EventType.CLEF) ?: eventHashOf()
  val keys = scoreQuery.getEvents(EventType.KEY_SIGNATURE) ?: eventHashOf()
  val times = scoreQuery.getEvents(EventType.TIME_SIGNATURE) ?: eventHashOf()
  markBarsClef(clefs, scoreQuery.numBars, map)
  markBarsKey(keys, scoreQuery, map)
  markBarsTime(times, scoreQuery, map)
  return map
}

private fun markBarsClef(
  eventHash: EventHash, numBars: Int,
  existing: MutableMap<EventAddress, HeaderEventMap>
) {
  val grouped = eventHash.toList().groupBy { it.first.eventAddress.staveId }

  grouped.forEach { (staveId, hash) ->
    markBarsForStave(hash.toMap(), EventType.CLEF, numBars, staveId, existing)
  }
}

/* Each header area will draw the event that's in effect, not the one exactly in that bar -
  so each event of particular type marks all the bars up to the next event
 */
private fun markBarsForStave(
  eventHash: EventHash, eventType: EventType, numBars: Int,
  staveId: StaveId,
  existing: MutableMap<EventAddress, HeaderEventMap>
) {

  val byBar = eventHash.toList().groupBy { it.first.eventAddress.barNum }
  var currentEvent: Event? = null
  (1..numBars).forEach { bar ->
    val address = eas(bar, dZero(), staveId)

    byBar[bar]?.let { thisBarHash ->
      val event = thisBarHash.toMap()[EMK(eventType, address)] ?: currentEvent
      event?.let {
        addToMap(existing, address, event)
      }
      currentEvent = thisBarHash.last().second
    } ?: run {
      currentEvent?.let {
        addToMap(existing, address, it)
      }
    }
  }
}

private fun markBarsKey(
  eventHash: EventHash, scoreQuery: ScoreQuery,
  existing: MutableMap<EventAddress, HeaderEventMap>
) {

  val transpositions = getTranspositions(scoreQuery)
  val staves = getNonPercussionStaves(scoreQuery)
  val transpose = !getOption<Boolean>(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, scoreQuery)

  staves.forEach { staveId ->
    val hash = transposeKeySignatures(transpose, staveId, transpositions, eventHash)
    markBarsForStave(hash, EventType.KEY_SIGNATURE, scoreQuery.numBars, staveId, existing)
  }
}

private fun transposeKeySignatures(
  transpose: Boolean,
  staveId: StaveId,
  transpositions: Map<PartNum, Int>,
  eventHash: EventHash
): EventHash {
  val transposition = if (transpose) transpositions[staveId.main] else null

  return eventHash.map { (key, event) ->
    val transposedEvent = transposition?.let {
      event.getParam<Int>(EventParam.SHARPS)?.let { sharps ->
        KeySignature(transposeKey(sharps, -transposition)).toEvent()
      }
    } ?: event
    key.copy(eventAddress = key.eventAddress.copy(staveId = staveId)) to transposedEvent
  }.toMap()
}

private fun getTranspositions(scoreQuery: ScoreQuery): Map<PartNum, Int> {
  return scoreQuery.allParts(true).map { part ->
    part to (scoreQuery.getParam<Int>(
      EventType.INSTRUMENT, EventParam.TRANSPOSITION,
      EventAddress(1, staveId = StaveId(part, 0))
    ) ?: 0)
  }.toMap()
}

private fun getNonPercussionStaves(scoreQuery: ScoreQuery): Iterable<StaveId> {
  return scoreQuery.allParts(true).filterNot {
    scoreQuery.getParam<Boolean>(
      EventType.INSTRUMENT,
      EventParam.PERCUSSION,
      eas(1, dZero(), StaveId(it, 0))
    ) ?: true
  }.flatMap { part ->
    (1..scoreQuery.numStaves(part)).map { s -> StaveId(part, s) }
  }
}

private fun markBarsTime(
  eventHash: EventHash,
  scoreQuery: ScoreQuery,
  existing: MutableMap<EventAddress, HeaderEventMap>
) {

  var actual = eventHash
  actual = replaceHiddenAtStart(actual)
  actual = actual.filterNot { it.value.isTrue(EventParam.HIDDEN) }

  /* Unlike clefs and key signatures, time signatures only appear in the headers if they
    are declared in that bar
   */
  scoreQuery.getAllStaves(true).forEach { stave ->
    val badged =
      actual.map { it.key.copy(eventAddress = it.key.eventAddress.copy(staveId = stave)) to it.value }
        .toMap()
    badged.forEach { (k, v) ->
      addToMap(existing, k.eventAddress, v)
    }
  }
}

/* The TS of an upbeat bar isn't drawn - the real time signature is at bar 2 */
private fun replaceHiddenAtStart(eventHash: EventHash): EventHash {
  return eventHash[EMK(
    EventType.TIME_SIGNATURE,
    ez(1)
  )]?.let { firstTs ->
    if (firstTs.isTrue(EventParam.HIDDEN)) {
      eventHash[EMK(
        EventType.TIME_SIGNATURE,
        ez(2)
      )]?.let { mainTs ->
        eventHash.plus(
          EMK(EventType.TIME_SIGNATURE, ez(1)) to mainTs.addParam(
            EventParam.HIDDEN,
            false
          )
        )
      }
    } else {
      null
    }
  } ?: eventHash
}

private fun addToMap(
  existing: MutableMap<EventAddress, HeaderEventMap>,
  address: EventAddress, event: Event
) {
  val headerEventMap = existing[address]?.toMutableMap() ?: headerEventMapOf()
  headerEventMap.put(event.eventType, event)
  existing.put(address, headerEventMap)
}