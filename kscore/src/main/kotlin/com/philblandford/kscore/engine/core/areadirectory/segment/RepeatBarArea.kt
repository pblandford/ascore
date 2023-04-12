package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DiagonalArgs
import com.philblandford.kscore.engine.core.area.factory.DotArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.header.numberArea
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.types.*


internal fun DrawableFactory.repeatBarArea(repeatBarType: RepeatBarType): Area? {
  if (repeatBarType == RepeatBarType.TWO_END) {
    return null
  }
  return getDrawableArea(DotArgs(REPEAT_BAR_DOT_WIDTH, REPEAT_BAR_DOT_WIDTH))?.let { dot ->
    getDrawableArea(
      DiagonalArgs(
        REPEAT_BAR_WIDTH, REPEAT_BAR_HEIGHT,
        REPEAT_BAR_THICKNESS, true
      )
    )?.let { line ->
      val num = if (repeatBarType == RepeatBarType.ONE) 1 else 2
      var area = Area(tag = "RepeatBar", event = Event(EventType.REPEAT_BAR,
        paramMapOf(EventParam.NUMBER to num))).addArea(dot).addArea(line)
      var width = REPEAT_BAR_WIDTH
      if (num == 2) {
        area = area.addArea(line, Coord(BLOCK_HEIGHT * 2, 0))
        width += BLOCK_HEIGHT * 2
        numberArea(2, REPEAT_BAR_HEIGHT)?.let { na ->
          area = area.addAbove(na, BLOCK_HEIGHT * 4, width / 2 - na.width / 2)
        }
      }
      area = area.addArea(
        dot,
        Coord(width - REPEAT_BAR_DOT_WIDTH, REPEAT_BAR_HEIGHT - REPEAT_BAR_DOT_WIDTH)
      )

      area
    }
  }
}