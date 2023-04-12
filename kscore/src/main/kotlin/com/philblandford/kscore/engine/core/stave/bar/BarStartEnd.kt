package com.philblandford.kscore.engine.core.stave.bar

import com.philblandford.kscore.engine.core.ResolvedBarGeography
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarEndArea
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarEndAreaPair
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarStartAreaPair
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventType


internal fun addBarStartEnd(
  area: Area, barStartArea: BarStartAreaPair,
  barEndArea: BarEndAreaPair, barGeography: ResolvedBarGeography,
  eventAddress: EventAddress
): Area {
  var barStart = if (barGeography.first) barStartArea.startStave else barStartArea.notStartStave
  barStart = barStart.copy(base = barStart.base.transformEventAddress { _, _ -> eventAddress })

  var barEnd = if (barGeography.last) barEndArea.endStave else barEndArea.notEndStave
  barEnd = transformEndBarAddress(
    barEnd,
    eventAddress
  )
  var newArea = area
  newArea = newArea.addArea(barStart.base, eventAddress = eventAddress)
  newArea = newArea.addArea(
    barEnd.base, Coord(barGeography.width - barGeography.barEndGeography.width, 0),
    eventAddress
  )
  return newArea
}

private fun transformEndBarAddress(barEnd: BarEndArea, eventAddress: EventAddress): BarEndArea {
  return barEnd.copy(base = barEnd.base.transformEventAddress { ea, event ->
    when (event?.eventType) {
      EventType.REPEAT_END -> eventAddress.staveless()
      else -> eventAddress.inc()
    }
  })
}