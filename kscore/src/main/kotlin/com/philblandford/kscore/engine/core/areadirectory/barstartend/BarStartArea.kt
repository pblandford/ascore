package com.philblandford.kscore.engine.core.areadirectory.barstartend

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.header.keySignatureArea
import com.philblandford.kscore.engine.core.areadirectory.header.timeSignatureArea
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.REPEAT_DOT_WIDTH
import com.philblandford.kscore.engine.core.representation.STAVE_HEADER_GAP
import com.philblandford.kscore.engine.time.timeSignature
import com.philblandford.kscore.engine.types.BarLineType
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventType

data class BarStartAreaPair(
  val startStave: BarStartArea = BarStartArea(Area()),
  val notStartStave: BarStartArea = BarStartArea(Area())
)

private val emptyPair = BarStartAreaPair()

fun DrawableFactory.barStartAreaPair(events: Map<EventType, Event>): BarStartAreaPair {
  if (events.isEmpty()) {
    return emptyPair
  }
  val startStave = barStartAreaStartStave(events)
  val notStartStave = barStartAreaNotStartStave(events)
  return BarStartAreaPair(startStave, notStartStave)
}

private fun DrawableFactory.barStartAreaStartStave(events: Map<EventType, Event>): BarStartArea {
  var area = Area(tag = "BarStart")
  area = addRepeatArea(area, events)
  return BarStartArea(area)
}

private fun DrawableFactory.barStartAreaNotStartStave(events: Map<EventType, Event>): BarStartArea {
  var area = Area(tag = "BarStart")
  area = addRepeatArea(area, events)
  area = addKeySignatureArea(area, events)
  area = addTimeSignatureArea(area, events)
  return BarStartArea(area)
}

private fun DrawableFactory.addRepeatArea(area: Area, events: Map<EventType, Event>): Area {
  return events[EventType.REPEAT_START]?.let { _ ->
    area.addArea(
      repeatDotArea().copy(tag = "StartRepeat"),
      Coord(barLineWidth(BarLineType.START) + REPEAT_DOT_WIDTH, (BLOCK_HEIGHT * 2.5).toInt())
    )
  } ?: area
}

private fun DrawableFactory.addTimeSignatureArea(area: Area, events: Map<EventType, Event>): Area {
  return events[EventType.TIME_SIGNATURE]?.let {
    timeSignature(it)?.let {
      area.addRight(timeSignatureArea(it), STAVE_HEADER_GAP)
    }
  } ?: area
}

private fun DrawableFactory.addKeySignatureArea(area: Area, events: Map<EventType, Event>): Area {
  return events[EventType.KEY_SIGNATURE]?.let {
    keySignatureArea(it)?.let {
      area.addRight(it, STAVE_HEADER_GAP)
    }
  } ?: area
}