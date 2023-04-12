package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.areadirectory.segment.ornamentAccidental
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.ORNAMENT_HEIGHT
import com.philblandford.kscore.engine.types.*

object LongTrillDecorator : LineDecorator {

  override fun DrawableFactory.getArea(event: Event, width: Int, lineDescriptor: LineDescriptor): Area? {

    var area = Area(tag = "LongTrill", event = event, addressRequirement = AddressRequirement.EVENT)

    lineDescriptor.start?.let {
      getSymbol()?.let { symbol ->
        area = area.addArea(symbol)
      }
    }
    val yPos = 0
    getTrillLine(width - area.width)?.let { line ->
      area = area.addRight(line, y = yPos)
    }
    area = getAccidental(width, event, area)
    return area
  }

  override fun isUp(eventAddress: EventAddress, event: Event): Boolean {
    return event.getNumber() > 0
  }

  private fun Event.getNumber(): Int {
    return getParam<Int>(EventParam.NUMBER) ?: 8
  }

  private fun DrawableFactory.getSymbol(): Area? {
    return getDrawableArea(ImageArgs("ornament_trill", INT_WILD, ORNAMENT_HEIGHT))
  }

  private fun DrawableFactory.getTrillLine(width:Int): Area? {
    var trillLine = Area(tag = "LongTrillLine")
    getDrawableArea(ImageArgs("ornament_trill_part", INT_WILD, ORNAMENT_HEIGHT))?.let { part ->
      while (trillLine.width < width - part.width) {
        trillLine = trillLine.addRight(part)
      }
    }
    return trillLine
  }

  private fun DrawableFactory.getAccidental(width: Int, event: Event, mainArea:Area):Area {
    return event.getParam<Accidental>(EventParam.ACCIDENTAL_ABOVE)?.let { aAbove ->
      ornamentAccidental(aAbove)?.let { aArea ->
        mainArea.addArea(aArea.copy(tag = "LongTrillAccidental"), Coord(width/2, -aArea.height - BLOCK_HEIGHT))
      }
    } ?: mainArea
  }
}