package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.StaveId
import com.philblandford.kscore.engine.types.StavePositionFinder
import com.philblandford.kscore.engine.types.sZero

interface Decorator {
  fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area,
    drawableFactory: DrawableFactory
  ): Area
}

fun DrawableFactory.decorateStave(
  eventHash: EventHash, stavePositionFinder: StavePositionFinder, staveArea: Area
): Area {

  val staveId = stavePositionFinder.staveId
  val newHash = modifyAddresses(eventHash, staveId).filter {
    it.key.eventAddress.staveId == stavePositionFinder.staveId
  }
  val grouped = groupEvents(newHash)
  var areaCopy = staveArea

  decoratorValues.forEach { decorator ->
    grouped[decorator]?.let { events ->
      areaCopy = decorator.decorate(events, stavePositionFinder, areaCopy, this)
    }
  }
  return areaCopy
}

private fun groupEvents(eventHash: EventHash): Map<Decorator, EventHash> {
  val map = mutableMapOf<Decorator, EventHash>()
  val grouped = eventHash.toList().groupBy { it.first.eventType }.toMap()

  decorators.forEach { (et, decorator) ->
    grouped[et]?.let { events ->
      val all = map[decorator] ?: eventHashOf()
      map[decorator] = all.plus(events)
    }
  }
  return map
}

private fun modifyAddresses(eventHash: EventHash, staveId: StaveId): EventHash {
  val newHash = eventHash.toMutableMap()
  eventHash.entries.forEach {
    if (it.key.eventAddress.staveId == sZero()) {
      newHash.remove(it.key)
      newHash[it.key.copy(eventAddress = it.key.eventAddress.copy(staveId = staveId))] = it.value
    }
  }
  return newHash.toMap()
}


private val decorators = listOf(
  EventType.OPTION to BarNumberDecorator,
  EventType.TUPLET to TupletDecorator,
  EventType.TIE to TieDecorator,
  EventType.SLUR to SlurDecorator,
  EventType.PAUSE to PauseDecorator,
  EventType.LYRIC to LyricDecorator,
  EventType.BREAK to BreakDecorator,
  EventType.DYNAMIC to DynamicDecorator,
  EventType.WEDGE to WedgeDecorator,
  EventType.EXPRESSION_TEXT to ExpressionDecorator,
  EventType.EXPRESSION_DASH to ExpressionDashDecorator,
  EventType.FERMATA to FermataDecorator,
  EventType.TEMPO to TempoTextDecorator,
  EventType.TEMPO_TEXT to TempoTextDecorator,
  EventType.HARMONY to HarmonyDecorator,
  EventType.LONG_TRILL to LongTrillDecorator,
  EventType.NAVIGATION to NavigationDecorator,
  EventType.OCTAVE to OctaveDecorator,
  EventType.PEDAL to PedalDecorator,
  EventType.VOLTA to VoltaDecorator,
  EventType.REHEARSAL_MARK to RehearsalMarkDecorator,
  EventType.GLISSANDO to GlissandoDecorator
)

val decoratorTypes = decorators.map { it.first }
val decoratorValues = decorators.map { it.second }.distinct()
