package com.philblandford.kscore.engine.core.areadirectory.barstartend

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DotArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.core.area.factory.RectArgs
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.util.blue

fun DrawableFactory.placeholderBarLine(height: Int): Area? {
  return getDrawableArea(
    LineArgs(
      height, false, color = blue(), dashWidth = BLOCK_HEIGHT,
      dashGap = BLOCK_HEIGHT, export = false
    )
  )?.copy(tag = "PlaceHolderLine", event = Event(EventType.PLACE_HOLDER),
    addressRequirement = AddressRequirement.EVENT)
}

fun DrawableFactory.repeatDotArea(): Area {
  var base = Area(event = Event(EventType.REPEAT_START), addressRequirement = AddressRequirement.EVENT)
  val dot = getDrawableArea(DotArgs(REPEAT_DOT_WIDTH, REPEAT_DOT_WIDTH))!!
  base = base.addArea(dot)
  base = base.addArea(dot, Coord(0, BLOCK_HEIGHT * 2))
  return base
}

fun DrawableFactory.barLineArea(barLineType: BarLineType, height: Int): Area {
  val area = when (barLineType) {
    BarLineType.NORMAL -> normalBarLineArea(height)
    BarLineType.DOUBLE -> doubleBarLineArea(height)
    BarLineType.FINAL -> finalBarLineArea(height)
    BarLineType.START -> startBarLineArea(height)
    else -> normalBarLineArea(height)
  }
  return area.copy(event = Event(EventType.BARLINE, paramMapOf(EventParam.TYPE to barLineType)),
    addressRequirement = AddressRequirement.EVENT)
}

fun barLineWidth(barLineType: BarLineType): Int {
  return when (barLineType) {
    BarLineType.NORMAL -> 0
    BarLineType.DOUBLE -> LINE_THICKNESS * 2 + DOUBLE_BAR_LINE_GAP
    BarLineType.FINAL -> FINAL_BAR_LINE_THICK + LINE_THICKNESS + DOUBLE_BAR_LINE_GAP
    BarLineType.START -> FINAL_BAR_LINE_THICK
    else -> 0
  }
}

private fun DrawableFactory.normalBarLineArea(height: Int): Area {
  return getDrawableArea(LineArgs(height, horizontal = false)) ?: Area()
}


private fun DrawableFactory.finalBarLineArea(height: Int): Area {
  var base = Area()
  val thinBit = normalBarLineArea(height)
  val thickBit = getDrawableArea(RectArgs(FINAL_BAR_LINE_THICK, height, true)) ?: Area()
  base = base.addArea(thinBit)
  base = base.addRight(thickBit, gap = DOUBLE_BAR_LINE_GAP)
  return base
}


private fun DrawableFactory.doubleBarLineArea(height: Int): Area {
  var base = Area()
  val thinBit = normalBarLineArea(height)
  base = base.addArea(thinBit)
  base = base.addRight(thinBit, gap = DOUBLE_BAR_LINE_GAP)
  return base
}

private fun DrawableFactory.startBarLineArea(height: Int): Area {
  var base = Area()
  val thinBit = normalBarLineArea(height)
  val thickBit = getDrawableArea(RectArgs(FINAL_BAR_LINE_THICK, height, true)) ?: Area()
  base = base.addArea(thickBit, Coord(thickBit.width / 2))
  base = base.addRight(thinBit, gap = DOUBLE_BAR_LINE_GAP)
  return base
}