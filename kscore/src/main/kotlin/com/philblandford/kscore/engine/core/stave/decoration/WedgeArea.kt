package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DiagonalArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.LINE_THICKNESS
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.WedgeType

fun DrawableFactory.wedgeArea(event: Event, width:Int): Area? {

  return event.getParam<WedgeType>(EventParam.TYPE)?.let { type ->
    when (type) {
      WedgeType.CRESCENDO -> crescendo(event, width)
      WedgeType.DIMINUENDO -> diminuendo(event, width)
    }
  }
}


private fun DrawableFactory.crescendo(event: Event, width:Int):Area? {

  return getDrawableArea(DiagonalArgs(width, BLOCK_HEIGHT, LINE_THICKNESS, true))?.let { top ->
    getDrawableArea(DiagonalArgs(width, BLOCK_HEIGHT, LINE_THICKNESS, false))?.let { bottom ->
      Area(tag = "Wedge", event = event, addressRequirement = AddressRequirement.EVENT).addArea(top)
        .addBelow(bottom)
    }
  }
}

private fun DrawableFactory.diminuendo(event: Event, width:Int):Area? {
  return getDrawableArea(DiagonalArgs(width, BLOCK_HEIGHT, LINE_THICKNESS, false))?.let { top ->
    getDrawableArea(DiagonalArgs(width, BLOCK_HEIGHT, LINE_THICKNESS, true))?.let { bottom ->
      Area(tag = "Wedge", event = event, addressRequirement = AddressRequirement.EVENT).addArea(top)
        .addBelow(bottom)
    }
  }
}