package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam


internal fun Area.addEventArea(
  event: Event?, area: Area, eventAddress: EventAddress,
  coord: Coord = Coord(), ignoreHardStart: Boolean = false
): Area {
  val realCoord = if (!ignoreHardStart) event?.getParam<Coord>(EventParam.HARD_START)?.let { shift ->
    coord.plus(shift)
  } ?: coord
  else coord
  return addArea(area.copy(event = event), realCoord, eventAddress)
}