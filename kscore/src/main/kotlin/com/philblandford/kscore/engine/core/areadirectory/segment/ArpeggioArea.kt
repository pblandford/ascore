package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.INT_WILD

internal fun DrawableFactory.arpeggioArea(height:Int): Area? {

  return getDrawableArea(ImageArgs("arpeggio_part", INT_WILD, BLOCK_HEIGHT*2))?.let { part ->
    var base = Area(tag = "Arpeggio", event = Event(EventType.ARPEGGIO))
    repeat(height/part.height) {
      base = base.addBelow(part)
    }
    base
  }
}