package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.INT_WILD

fun DrawableFactory.staveJoinArea(height:Int, event:Event): Area? {

  var area = Area(tag = "StaveJoin", event = event)
  return getDrawableArea(LineArgs(height, false, STAVE_JOIN_THICKNESS))?.let { line ->
    getDrawableArea(ImageArgs("stave_join_top", INT_WILD, STAVE_JOIN_END_HEIGHT))?.let { top ->
      getDrawableArea(ImageArgs("stave_join_bottom", INT_WILD, STAVE_JOIN_END_HEIGHT))?.let { bottom ->
        area = area.addArea(line)
        area = area.addAbove(top)
        area = area.addBelow(bottom)
        area
      }
    }
  }
}