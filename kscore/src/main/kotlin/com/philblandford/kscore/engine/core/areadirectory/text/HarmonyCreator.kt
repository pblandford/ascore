package com.philblandford.kscore.engine.core.areadirectory.text

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.stave.decoration.HarmonyDecorator
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.option.getOption

fun DrawableFactory.createHarmonyWidths(scoreQuery: ScoreQuery):Lookup<Int> {

  val areas = createHarmonyAreas(scoreQuery)
  val grouped = areas.toList().groupBy { Pair(it.first.barNum, it.first.offset) }
  return grouped.mapNotNull { (k, entries) ->
    val width = entries.maxByOrNull { it.second.width }?.second?.width
    width?.let { ez(k.first, k.second) to it + BLOCK_HEIGHT*2 }
  }.toMap()
}

private fun DrawableFactory.createHarmonyAreas(scoreQuery: ScoreQuery):Lookup<Area> {
  val font =
    getOption<String>(EventParam.OPTION_HARMONY_FONT, scoreQuery)
  val size = getOption<Int>(EventParam.OPTION_HARMONY_SIZE, scoreQuery)
  return scoreQuery.getEvents(EventType.HARMONY)?.mapNotNull { (key, harmony) ->
    HarmonyDecorator.getArea(harmony, font, size, this)?.let { key.eventAddress to it }
  }?.toMap() ?: mapOf()
}
