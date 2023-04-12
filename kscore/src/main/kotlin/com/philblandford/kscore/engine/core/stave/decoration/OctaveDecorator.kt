package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.core.representation.OCTAVE_HEIGHT
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.INT_WILD

object OctaveDecorator : LineDecorator {

  override fun DrawableFactory.getArea(event: Event, width: Int, lineDescriptor: LineDescriptor): Area? {

    var area = Area(tag = "Octave", event = event, addressRequirement = AddressRequirement.EVENT)

    getSymbol(event)?.let { symbol ->
      area = area.addArea(symbol)
    }

    val yPos = if (event.getNumber() > 0) 0 else OCTAVE_HEIGHT
    getDrawableArea(LineArgs(width - area.width, true, dashWidth = 10, dashGap = 20))?.let { line ->
      area = area.addRight(line.copy(tag = "OctaveLine"), y = yPos)
    }
    lineDescriptor.end?.let {
      getDrawableArea(LineArgs(OCTAVE_HEIGHT, false))?.let { endLine ->
        area = area.addRight(endLine)
      }
    }
    return area
  }

  override fun isUp(eventAddress: EventAddress, event: Event): Boolean {
    return event.getNumber() > 0
  }

  private fun Event.getNumber(): Int {
    return getParam<Int>(EventParam.NUMBER) ?: 8
  }

  private fun DrawableFactory.getSymbol(event: Event): Area? {
    val key = when (event.getNumber()) {
      1 -> "octave_8va"
      2 -> "octave_15va"
      (-1) -> "octave_8mb"
      (-2) -> "octave_15mb"
      else -> ""
    }
    return getDrawableArea(ImageArgs(key, INT_WILD, OCTAVE_HEIGHT))
  }
}