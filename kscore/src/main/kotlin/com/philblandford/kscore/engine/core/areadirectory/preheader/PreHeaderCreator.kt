package com.philblandford.kscore.engine.core.areadirectory.preheader

import com.philblandford.kscore.engine.core.PreHeaderGeography
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.option.getOption
import com.philblandford.kscore.util.toImmutableMap

fun DrawableFactory.createPreHeaders(scoreQuery: ScoreQuery): Pair<Lookup<PreHeaderGeography>, Lookup<PreHeaderArea>>? {

  val events = (scoreQuery.allParts(true)).map {
    Pair(it, getEventsForBar(ea(1).copy(staveId = StaveId(it, 0)), scoreQuery))
  }.toMap()

  val firstHeaders = getHeaders(events, LabelType.FULL, showFullPartName(scoreQuery))
  val otherHeaders = getHeaders(events, LabelType.SHORT, showAbbreviation(scoreQuery))

  val firstGeog = createGeog(firstHeaders.map { it.value })
  val otherGeog = createGeog(otherHeaders.map { it.value })

  val areas = (1..scoreQuery.numBars).flatMap { bar ->
    scoreQuery.allParts(true).map { part ->
      val addr = ea(bar).copy(staveId = StaveId(part, 0))
      if (bar == 1) {
        firstHeaders[part]?.let { Pair(addr, it) }
      } else {
        otherHeaders[part]?.let { Pair(addr, it) }
      }
    }
  }.filterNotNull().toMap()

  val geogs = (1..scoreQuery.numBars).flatMap { bar ->
    scoreQuery.allParts(true).map { part ->
      val addr = ea(bar).copy(staveId = StaveId(part, 0))
      if (bar == 1) {
        addr to firstGeog
      } else {
        addr to otherGeog
      }
    }
  }.toMap()
  return Pair(geogs, areas)
}

private fun showFullPartName(scoreQuery: ScoreQuery):Boolean {
  return !scoreQuery.singlePartMode() && getOption(EventParam.OPTION_SHOW_PART_NAME, scoreQuery)
}

private fun showAbbreviation(scoreQuery: ScoreQuery):Boolean {
  return !scoreQuery.singlePartMode() && getOption(EventParam.OPTION_SHOW_PART_NAME_START_STAVE, scoreQuery)
}

private fun DrawableFactory.getHeaders(
  events: Map<Int, Map<EventType, Event>>,
  labelType: LabelType,
  yes: Boolean
): Map<Int, PreHeaderArea> {
  return events.map { (part, evs) ->
    preHeaderArea(
      evs, ea(1).copy(staveId = StaveId(part, 0)),
      if (yes) labelType else LabelType.NONE
    )?.let {
      part to it
    }
  }.filterNotNull().toMap()
}

private fun createGeog(headers: Iterable<PreHeaderArea>): PreHeaderGeography {
  val textWidth = headers.maxByOrNull { it.preHeaderGeography.textWidth }?.preHeaderGeography?.textWidth ?: 0
  val joinWidth = headers.maxByOrNull { it.preHeaderGeography.joinWidth }?.preHeaderGeography?.joinWidth ?: 0
  return PreHeaderGeography(textWidth, joinWidth)
}

private fun getEventsForBar(eventAddress: EventAddress, scoreQuery: ScoreQuery): Map<EventType, Event> {
  return setOf(EventType.PART, EventType.STAVE_JOIN).mapNotNull { type ->
    scoreQuery.getEvent(type, eventAddress)?.let { type to it }
  }.toMap()
}
