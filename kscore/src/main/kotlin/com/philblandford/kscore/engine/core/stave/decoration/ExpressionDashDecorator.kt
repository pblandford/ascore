package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.EXPRESSION_DASH_GAP
import com.philblandford.kscore.engine.core.representation.EXPRESSION_DASH_LENGTH
import com.philblandford.kscore.engine.core.representation.TEXT_SIZE
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam

object ExpressionDashDecorator : LineDecorator {

  override fun DrawableFactory.getArea(event: Event, width: Int, lineDescriptor: LineDescriptor): Area? {

    return getTextArea(event)?.let { textArea ->
      getLineArea(width - textArea.width, event)?.let { lineArea ->
        Area(
          tag = "ExpressionDash",
          event = event,
          addressRequirement = AddressRequirement.EVENT
        ).addArea(textArea).addRight(
          lineArea,
          gap = BLOCK_HEIGHT * 2, y = textArea.height / 2
        )
      }
    }
  }

  private fun DrawableFactory.getTextArea(event: Event): Area? {
    return event.getParam<String>(EventParam.TEXT)?.let { text ->
      getDrawableArea(
        TextArgs(
          text, font = event.getParam<String>(EventParam.FONT) ?: TextType.EXPRESSION.getFont(),
          size = event.getParam<Int>(EventParam.TEXT_SIZE) ?: TEXT_SIZE
        )
      )
    }
  }

  private fun DrawableFactory.getLineArea(width: Int, event: Event): Area? {
    val extra = event.getParam<Coord>(EventParam.DASH_ADJUSTMENT)?.x ?: 0
    return getDrawableArea(
      LineArgs(
        width,
        true,
        dashWidth = EXPRESSION_DASH_LENGTH,
        dashGap = EXPRESSION_DASH_GAP + extra
      )
    )
  }
}