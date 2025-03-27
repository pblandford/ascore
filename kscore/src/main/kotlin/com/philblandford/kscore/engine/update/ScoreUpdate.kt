package com.philblandford.kscore.engine.update

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevel
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLoge

internal data class ScoreDiff(
  val changedLines: List<Int> = listOf(),
  val changedBars: List<EventAddress> = listOf(),
  val recreate: Boolean = false,
  val createHeaders: Boolean = false,
  val createParts: Boolean = false,
  val pagesOnly: Boolean = false
)

private data class ScoreLevelChange(val num: Int, val old: ScoreLevel, val new: ScoreLevel)

internal fun Score.diff(other: Score): ScoreDiff {

  if (recreate(other)) {
    return ScoreDiff(listOf(), listOf(), true)
  }

  if (needPagesOnly(other)) {
    return ScoreDiff(pagesOnly = true)
  }

  val lines = lineDiff(other)
  val bars = getChangedBars(other)

  val createHeaders = needCreateHeaders(other)

  if (bars.isEmpty() && (lines.isEmpty() || lines.toList() == listOf(0))) {
    return getOptionDiff(other).copy(createHeaders = createHeaders)
  }

  return ScoreDiff(
    lines,
    bars,
    createHeaders = createHeaders,
    createParts = lines.isNotEmpty() || bars.isNotEmpty()
  )
}

private fun compareOption(old: Score, new: Score, option: EventParam): Boolean {
  return old.eventMap.getParam<Any>(EventType.OPTION, option) == new.eventMap.getParam<Any>(
    EventType.OPTION,
    option
  )
}

private fun Score.needPagesOnly(oldScore: Score): Boolean {
  val events = listOf(
    EventType.TITLE, EventType.COMPOSER, EventType.SUBTITLE,
    EventType.LYRICIST, EventType.FOOTER_LEFT, EventType.FOOTER_RIGHT, EventType.FOOTER_CENTER
  )
  return events.any { eventMap.eventChanged(oldScore.eventMap, it) }
}

private fun Score.needCreateParts(oldScore: Score): Boolean {
  val events = listOf(
    EventType.STAVE_JOIN, EventType.TITLE, EventType.COMPOSER, EventType.SUBTITLE,
    EventType.LYRICIST
  )
  return events.any { eventMap.eventChanged(oldScore.eventMap, it) }
}

private fun Score.getOptionDiff(oldScore: Score): ScoreDiff {
  val noRecreate = ScoreDiff(listOf(), listOf(), false, false, false)
  val createParts = ScoreDiff(listOf(), listOf(), false, false, true)


  if (getParam<PartNum>(EventType.UISTATE, EventParam.SELECTED_PART)
    != oldScore.getParam(EventType.UISTATE, EventParam.SELECTED_PART)
  ) {
    return createParts
  }

  if (getParam<EventAddress>(EventType.UISTATE, EventParam.MARKER_POSITION)
    != oldScore.getParam(EventType.UISTATE, EventParam.MARKER_POSITION)
  ) {
    return noRecreate
  }

  for (type in listOf(EventType.UISTATE, EventType.LAYOUT, EventType.BREAK)) {
    ksLoge("optionDiff pondering $type")
    if (eventMap.eventChanged(oldScore.eventMap, type)) {
      ksLoge("optionDiff changed $type")
      return createParts
    }
  }

  subLevels.zip(oldScore.subLevels) { a, b ->
    for (type in listOf(EventType.LAYOUT, EventType.PLAYBACK_STATE)) {
      if (a.eventMap.getEvent(type) != b.eventMap.getEvent(type)) {
        return noRecreate
      }
    }
    if (a.eventMap.eventChanged(b.eventMap, EventType.BREAK)) {
      return createParts
    }
  }

  if (needCreateParts(oldScore)) {
    return createParts
  }

  return ScoreDiff(listOf(), listOf(), false)
}

private fun Score.recreate(other: Score): Boolean {
  if (selectedPart() != other.selectedPart()) {
    return true
  }

  if (subLevels.size != other.subLevels.size) {
    return true
  }
  subLevels.zip(other.subLevels).forEach { (oldPart, newPart) ->
    if (oldPart.subLevels.size != newPart.subLevels.size) {
      return true
    }
    oldPart.subLevels.zip(newPart.subLevels).forEach { (oldStave, newStave) ->
      if (oldStave.subLevels.size != newStave.subLevels.size) {
        return true
      }
    }
  }

  return false
}

private fun Score.needCreateHeaders(other: Score): Boolean {
  for (type in listOf(
    EventType.KEY_SIGNATURE, EventType.TIME_SIGNATURE,
    EventType.BARLINE, EventType.REPEAT_START, EventType.REPEAT_END
  )) {
    if (eventMap.eventChanged(other.eventMap, type)) {
      return true
    }
  }

  if (!compareOption(this, other, EventParam.OPTION_SHOW_TRANSPOSE_CONCERT) ||
    !compareOption(this, other, EventParam.OPTION_SHOW_PART_NAME_START_STAVE) ||
    !compareOption(this, other, EventParam.OPTION_SHOW_PART_NAME)
  ) {
    return true
  }

  if (getParam<PartNum>(EventType.UISTATE, EventParam.SELECTED_PART)
    != other.getParam(EventType.UISTATE, EventParam.SELECTED_PART)
  ) {
    return true
  }

  subLevels.zip(other.subLevels).forEach { (partA, partB) ->
    for (type in listOf(EventType.PART, EventType.INSTRUMENT)) {
      if (partA.eventMap.eventChanged(partB.eventMap, type)) {
        return true
      }
    }
  }


  subLevels.flatMap { it.subLevels }.zip(other.subLevels.flatMap { it.subLevels })
    .forEach { (staveA, staveB) ->
      if (staveA.eventMap.eventChanged(staveB.eventMap, EventType.CLEF)) {
        return true
      }
    }
  return false
}

private fun EventMap.eventChanged(new: EventMap, eventType: EventType): Boolean {
  return getEvents(eventType) != new.getEvents(eventType)
}

private fun Score.getChangedBars(new: Score): List<EventAddress> {
  val topLevel = eventMap.diff(
    new.eventMap, setOf(
      EventType.TITLE, EventType.SUBTITLE,
      EventType.COMPOSER, EventType.LYRICIST
    )
  ).map { EventAddress(it) }.filterNot { it.barNum == 0 }
  return topLevel + getChangedSubLevels(new).flatMap { partChange ->
    partChange.old.getChangedSubLevels(partChange.new).flatMap { staveChange ->
      val staveEventsThatEffectBars = staveChange.old.eventMap.diff(
        staveChange.new.eventMap,
        EventType.values().toSet() - EventType.GLISSANDO
      ).map {
        EventAddress(it, staveId = StaveId(partChange.num + 1, staveChange.num + 1))
      }
      staveEventsThatEffectBars + staveChange.old.getChangedSubLevels(staveChange.new)
        .map { barChange ->
          EventAddress(
            barChange.num + 1,
            staveId = StaveId(partChange.num + 1, staveChange.num + 1)
          )
        }
    }
  }
}


private fun Score.lineDiff(new: Score): List<Int> {
  val bars = mutableListOf<Int>()

  bars.addAll(eventMap.diff(new.eventMap, setOf(EventType.UISTATE)))
  getChangedSubLevels(new).forEach { partChange ->
    partChange.old.getChangedSubLevels(partChange.new).forEach { staveChange ->
      bars.addAll(staveChange.old.eventMap.diff(staveChange.new.eventMap))
    }
  }

  if (eventMap.eventChanged(new.eventMap, EventType.OPTION)) {
    bars.addAll(1..numBars)
  }

  subLevels.zip(new.subLevels).forEach { (partA, partB) ->
    if (partA.abbreviation != partB.abbreviation) {
      bars.addAll(1..numBars)
    } else if (partA.label != partB.label) {
      bars.add(1)
    } else if (partA.eventMap.eventChanged(partB.eventMap, EventType.LAYOUT)) {
      bars.addAll(1..numBars)
    }
  }

  return bars.distinct()
}

private fun EventMap.diff(other: EventMap, ignore: Set<EventType> = setOf()): Iterable<Int> {
  val thisEvents = getAllEvents().filterNot { ignore.contains(it.key.eventType) }
  val thatEvents = other.getAllEvents().filterNot { ignore.contains(it.key.eventType) }

  val lost = thisEvents.minus(thatEvents.keys)
  val gained = thatEvents.minus(thisEvents.keys)
  val diff = thisEvents.minus(lost.keys).toList().zip(thatEvents.minus(gained.keys).toList())
    .mapNotNull { (a, b) ->
      if (a.second != b.second) b else null
    }

  return lost.plus(gained).plus(diff).map { it.key.eventAddress.barNum }.distinct()
}

private fun ScoreLevel.getChangedSubLevels(new: ScoreLevel): Iterable<ScoreLevelChange> {
  return subLevels.withIndex().mapNotNull { iv ->
    new.subLevels.toList().getOrNull(iv.index)?.let { old ->
      if (iv.value != old) {
        ScoreLevelChange(iv.index, iv.value, old)
      } else null
    }
  }
}