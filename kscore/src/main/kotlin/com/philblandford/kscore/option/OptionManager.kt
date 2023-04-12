package com.philblandford.kscore.option

import com.philblandford.kscore.engine.core.area.cZero
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.EventParam.*

private val defaults = mapOf(
  OPTION_BAR_NUMBERING to BarNumbering.EVERY_SYSTEM,
  OPTION_BARS_PER_LINE to 0,
  OPTION_SHOW_MULTI_BARS to false,
  OPTION_SHOW_TRANSPOSE_CONCERT to false,
  OPTION_HIDE_EMPTY_STAVES to false,
  OPTION_SHOW_PART_NAME to true,
  OPTION_SHOW_PART_NAME_START_STAVE to false,
  LAYOUT_PAGE_WIDTH to PAGE_WIDTH,
  LAYOUT_PAGE_HEIGHT to PAGE_HEIGHT,
  LAYOUT_TOP_MARGIN to PAGE_TOP_MARGIN,
  LAYOUT_BOTTOM_MARGIN to PAGE_BOTTOM_MARGIN,
  LAYOUT_LEFT_MARGIN to PAGE_LEFT_MARGIN,
  LAYOUT_RIGHT_MARGIN to PAGE_RIGHT_MARGIN,
  LAYOUT_STAVE_GAP to STAVE_GAP,
  LAYOUT_SYSTEM_GAP to SYSTEM_GAP,
  OPTION_SHUFFLE_RHYTHM to false,
  OPTION_LOOP to false,
  OPTION_HARMONY to false,
  OPTION_HARMONY_INSTRUMENT to null,
  OPTION_LYRIC_FONT to "default",
  OPTION_HARMONY_FONT to "default",
  OPTION_LYRIC_SIZE to LYRIC_SIZE,
  OPTION_HARMONY_SIZE to HARMONY_SIZE,
  OPTION_LYRIC_OFFSET to cZero(),
  OPTION_LYRIC_OFFSET_BY_POSITION to listOf<Pair<Boolean, Int>>(),
  OPTION_HARMONY_OFFSET to cZero(),
  OPTION_LYRIC_POSITIONS to listOf<Pair<Int, Boolean>>()
)

fun isLayoutOption(eventParam: EventParam): Boolean {
  return eventParam.ordinal >= LAYOUT_PAGE_WIDTH.ordinal && eventParam.ordinal <= LAYOUT_SYSTEM_GAP.ordinal
}

fun <T> getOptionDefault(option: EventParam): T = defaults[option] as T

fun <T> getOption(option: EventParam, scoreQuery: ScoreQuery): T {
  val eventType = if (isLayoutOption(option)) EventType.LAYOUT else EventType.OPTION
  return scoreQuery.getParam(eventType, option, eZero()) ?: getOptionDefault(option)
}

fun <T> getOption(option: EventParam, optionEvent: Event): T {
  return optionEvent.getParam<T>(option) ?: getOptionDefault(option)
}

fun getAllDefaults(): ParamMap {
  return defaults
}

