package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.header.numberArea
import com.philblandford.kscore.engine.types.ez

internal fun DrawableFactory.pageFooterArea(num:Int, width:Int, height:Int): Area {
  var area = Area(width, height, tag = "PageFooter")
  numberArea(num, PAGE_NUMBER_HEIGHT)?.let {
    area = area.addArea(it.copy(tag = "PageNumber-$num"),
      Coord(width/2 - it.width/2, height/2 - it.height/2), ez(num)
    )
  }
  return area
}