package com.philblandford.kscore.engine.core.stave

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.core.areadirectory.header.numberArea
import com.philblandford.kscore.engine.core.representation.*

fun DrawableFactory.multiBarArea(barWidth: Int, num: Int): Area? {
  return getDrawableArea(LineArgs(MULTIBAR_VERTICAL_HEIGHT, false))?.let { vertical ->
    getDrawableArea(
      LineArgs(barWidth - MULTIBAR_OFFSET_X * 2, true, MULTIBAR_THICKNESS)
    )?.let { horizontal ->
      numberArea(num, MULTIBAR_NUMBER_HEIGHT)?.let { number ->
        val area = Area(
          tag = "Multibar", addressRequirement = AddressRequirement.SEGMENT,
          extra = num
        )
        area.addArea(vertical).addArea(horizontal, Coord(0, BLOCK_HEIGHT * 2))
          .addArea(vertical, Coord(horizontal.width, 0)).addArea(
            number,
            Coord(horizontal.width / 2 - number.width / 2, -MULTIBAR_NUMBER_OFFSET - number.height)
          )
      }
    }
  }
}