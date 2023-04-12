package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.engine.core.representation.TEXT_SIZE
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam

object ExpressionDecorator : UpDownDecorator {

  override fun DrawableFactory.getArea(event: Event): Area? {
    return event.getParam<String>(EventParam.TEXT)?.let { text ->
      getDrawableArea(
        TextArgs(
          text, font = event.getParam<String>(EventParam.FONT) ?: TextType.EXPRESSION.getFont(),
          size = event.getParam<Int>(EventParam.TEXT_SIZE) ?: TEXT_SIZE
        )
      )?.copy(
        event = event,
        addressRequirement = AddressRequirement.EVENT, tag = "expression"
      )
    }
  }

}