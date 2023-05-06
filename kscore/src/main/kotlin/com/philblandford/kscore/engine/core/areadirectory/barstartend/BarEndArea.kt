package com.philblandford.kscore.engine.core.areadirectory.barstartend

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.header.clefArea
import com.philblandford.kscore.engine.core.areadirectory.header.keySignatureArea
import com.philblandford.kscore.engine.core.areadirectory.header.timeSignatureArea
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.REPEAT_DOT_WIDTH
import com.philblandford.kscore.engine.core.representation.STAVE_HEADER_GAP
import com.philblandford.kscore.engine.time.timeSignature
import com.philblandford.kscore.engine.types.BarLineType
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventType


data class BarEndAreaPair(
  val endStave: BarEndArea = BarEndArea(Area()),
  val notEndStave: BarEndArea = BarEndArea(Area())
)

private val emptyPair = BarEndAreaPair()

fun DrawableFactory.barEndArea(events: Map<EventType, Event>): BarEndAreaPair {
  if (events.isEmpty()) return emptyPair
  val endStave = barEndAreaEndStave(events)
  val notEndStave = barEndAreaNotEndStave(events)
  return BarEndAreaPair(endStave, notEndStave)
}

private fun DrawableFactory.barEndAreaEndStave(events: Map<EventType, Event>): BarEndArea {
  var base = Area(tag = "BarEnd")
  base = addKeySignatureArea(base, events)
  base = addTimeSignatureArea(base, events)
  base = addClefArea(base, events)
  base = addEndRepeat(base, events)
  base = extendForBarLine(base, events)
  return BarEndArea(base)
}

private fun DrawableFactory.barEndAreaNotEndStave(events: Map<EventType, Event>): BarEndArea {
  var base = Area(tag = "BarEnd")
  base = addClefArea(base, events)
  base = addEndRepeat(base, events)
  base = extendForBarLine(base, events)
  return BarEndArea(base)
}

private fun DrawableFactory.addEndRepeat(area: Area, events: Map<EventType, Event>): Area {
  return events[EventType.REPEAT_END]?.let { event ->
    area.addRight(
      repeatDotArea().copy(tag = "EndRepeat", event = event),
      y = BLOCK_HEIGHT * 3 - (REPEAT_DOT_WIDTH / 2)
    )
  } ?: area
}

private fun extendForBarLine(area: Area, events: Map<EventType, Event>): Area {
  var extra = 0
  if (events[EventType.REPEAT_END] != null) extra = barLineWidth(BarLineType.FINAL)
  else events[EventType.BARLINE]?.let {
    extra = barLineWidth(it.subType as BarLineType) + 5
  }
  return if ((events[EventType.KEY_SIGNATURE] != null || events[EventType.TIME_SIGNATURE] != null) &&
    (events[EventType.REPEAT_END] == null && events[EventType.REPEAT_START] == null)
  ) {
    area.extendLeft(extra)
  } else {
    area.extendRight(extra)
  }
}

private fun DrawableFactory.addKeySignatureArea(area: Area, events: Map<EventType, Event>): Area {
  return events[EventType.KEY_SIGNATURE]?.let {
    keySignatureArea(it)?.let {
      area.addRight(it.extendRight(STAVE_HEADER_GAP))
    }
  } ?: area
}

private fun DrawableFactory.addTimeSignatureArea(area: Area, events: Map<EventType, Event>): Area {
  return events[EventType.TIME_SIGNATURE]?.let {
    timeSignature(it)?.let {
      area.addRight(timeSignatureArea(it).extendRight(STAVE_HEADER_GAP))
    }
  } ?: area
}

private fun DrawableFactory.addClefArea(area: Area, events: Map<EventType, Event>): Area {
  return events[EventType.CLEF]?.let { clef ->
    clefArea(clef)?.let { clefArea ->
      area.addRight(clefArea.extendRight(STAVE_HEADER_GAP).copy(extra = "end"))
    }
  } ?: area
}