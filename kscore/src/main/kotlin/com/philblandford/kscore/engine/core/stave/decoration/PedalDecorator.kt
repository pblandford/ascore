package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.LINE_THICKNESS
import com.philblandford.kscore.engine.core.representation.PEDAL_HEIGHT
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.INT_WILD
import com.philblandford.kscore.engine.types.PedalType

object PedalDecorator : LineDecorator {

  override fun DrawableFactory.getArea(event: Event, width: Int, lineDescriptor: LineDescriptor): Area? {
    return when (event.subType) {
      PedalType.STAR -> getAreaStar(width, event)
      else -> getAreaLine(width, event)
    }
  }

  private fun DrawableFactory.getAreaLine(width: Int, event: Event): Area? {
    return getSymbol()?.let { symbol ->

      getDrawableArea(LineArgs(width - symbol.width - BLOCK_HEIGHT / 2, true))?.let { line ->
        getDrawableArea(LineArgs(PEDAL_HEIGHT, false))?.let { endLine ->
          Area(tag = "Pedal", event = event).addArea(symbol).addRight(
            line, gap = BLOCK_HEIGHT / 2,
            y = PEDAL_HEIGHT - LINE_THICKNESS
          ).addRight(endLine)
        }
      }
    }
  }

  private fun DrawableFactory.getAreaStar(width: Int, event: Event): Area? {
    return getSymbol()?.let { symbol ->

      getDrawableArea(ImageArgs("pedal_star", INT_WILD, PEDAL_HEIGHT))?.let { star ->
        Area(tag = "Pedal", event = event).addArea(symbol).addArea(
          star, Coord(width - star.width, 0)
        )
      }
    }
  }


  override fun isUp(eventAddress: EventAddress, event: Event): Boolean {
    return false
  }

  private fun DrawableFactory.getSymbol(): Area? {
    return getDrawableArea(ImageArgs("pedal_start", INT_WILD, PEDAL_HEIGHT))
  }
}