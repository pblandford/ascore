package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.RectArgs
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.REHEARSAL_MARK_LINE_THICKNESS
import com.philblandford.kscore.engine.core.representation.REHEARSAL_MARK_SIZE
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.util.black

object RehearsalMarkDecorator : UpDownDecorator {

  override fun DrawableFactory.getArea(event: Event): Area? {
    return event.getParam<String>(EventParam.TEXT)?.let { text ->
      getDrawableArea(
        TextArgs(
          text, size = event.getParam<Int>(EventParam.TEXT_SIZE) ?: REHEARSAL_MARK_SIZE,
          font = event.getParam<String>(EventParam.FONT) ?: TextType.SYSTEM.getFont()
        )
      )?.let { textArea ->
        getDrawableArea(
          RectArgs(
            textArea.width + BLOCK_HEIGHT * 2, textArea.height + BLOCK_HEIGHT * 2,
            false, black(), REHEARSAL_MARK_LINE_THICKNESS
          )
        )?.let { rect ->
          Area(tag = "RehearsalMark", event = event).addArea(rect)
            .addArea(textArea, Coord(BLOCK_HEIGHT, BLOCK_HEIGHT))
        }
      }
    }
  }

  override fun isUp(eventAddress: EventAddress, event: Event): Boolean {
    return true
  }

  override fun getEventAddress(eventAddress: EventAddress): EventAddress {
    return eventAddress.staveless()
  }
}