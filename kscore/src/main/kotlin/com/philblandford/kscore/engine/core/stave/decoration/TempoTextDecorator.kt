package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.TEXT_SIZE
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.StavePositionFinder

object TempoTextDecorator : Decorator {
  override fun decorate(
    eventHash: EventHash, stavePositionFinder: StavePositionFinder,
    staveArea: Area,
    drawableFactory:DrawableFactory
  ): Area {
    var areaCopy = staveArea
    val grouped = eventHash.toList().groupBy { it.first.eventAddress }.map {
      it.key to it.value.map { it.first.eventType to it.second }.toMap()
    }
    grouped.forEach { (eventAddress, events) ->
      stavePositionFinder.getSlicePosition(eventAddress)?.let { pos ->
        var xPos = pos.start
        drawableFactory.getTextArea(events)?.let { area ->
          val yPos =
            areaCopy.getTopForRange(xPos, xPos + area.width) - BLOCK_HEIGHT - area.height
          areaCopy =
            areaCopy.addEventArea(area.event, area, eventAddress.staveless(), Coord(xPos, yPos))
          xPos += area.width + BLOCK_HEIGHT
        }
        drawableFactory.getTempoArea(events)?.let { area ->
          val yPos =
            areaCopy.getTopForRange(xPos, xPos + area.width) - BLOCK_HEIGHT - area.height
          areaCopy =
            areaCopy.addEventArea(area.event, area, eventAddress.staveless(), Coord(xPos, yPos))
        }
      }
    }
    return areaCopy
  }


  private fun DrawableFactory.getTempoArea(events: Map<EventType, Event>): Area? {
    return events[EventType.TEMPO]?.let { event ->
      tempoArea(event)
    }
  }

  private fun DrawableFactory.getTextArea(events: Map<EventType, Event>): Area? {

    return events[EventType.TEMPO_TEXT]?.let { event ->
      event.getParam<String>(EventParam.TEXT)?.let { text ->
        getDrawableArea(
          TextArgs(
            text,
            size = event.getParam<Int>(EventParam.TEXT_SIZE) ?: TEXT_SIZE,
            font = event.getParam<String>(EventParam.FONT) ?: TextType.SYSTEM.getFont()
          )
        )?.copy(
          tag = "TempoText",
          event = event,
          addressRequirement = AddressRequirement.EVENT
        )
      }
    }
  }

}