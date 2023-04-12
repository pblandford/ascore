package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.BREAK_HEIGHT
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.types.*

object BreakDecorator : Decorator {

  override fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area, drawableFactory: DrawableFactory
  ): Area {

    val address = ez(stavePositionFinder.getEndBar()).copy(staveId = stavePositionFinder.staveId)
    return eventHash[EMK(EventType.BREAK, address)]?.let { event ->
      drawableFactory.getImage(event)?.let { area ->
        val x = stavePositionFinder.getEndBars() - area.width - BLOCK_HEIGHT
        val y = -BREAK_HEIGHT*2
        staveArea.addArea(area, Coord(x, y), address.staveless())
      }
    } ?: staveArea
  }

  private fun DrawableFactory.getImage(event: Event): Area? {
    val key = if (event.subType == BreakType.PAGE) "break_page" else "break_system"
    val tag = if (event.subType == BreakType.PAGE) "PageBreak" else "SystemBreak"
    return getDrawableArea(ImageArgs(key, INT_WILD, BREAK_HEIGHT, export = false))?.copy(tag = tag,
      event = event, addressRequirement = AddressRequirement.EVENT)
  }
}