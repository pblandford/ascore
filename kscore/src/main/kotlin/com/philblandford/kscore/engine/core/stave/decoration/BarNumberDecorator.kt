package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.header.numberArea
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.option.getOption

object BarNumberDecorator : Decorator {

  override fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area,
    drawableFactory: DrawableFactory
  ): Area {
    var copy = staveArea
    val staveId = if (stavePositionFinder.singlePartMode) stavePositionFinder.staveId.copy(sub = 1)
    else StaveId(1, 1)
    val option =
      eventHash[EventMapKey(
        EventType.OPTION,
        eZero().copy(staveId = staveId)
      )]?.let {
        getOption<Any>(EventParam.OPTION_BAR_NUMBERING, it)
      } ?: BarNumbering.EVERY_SYSTEM
    val upBeatBar = stavePositionFinder.getScoreQuery().getEvent(EventType.HIDDEN_TIME_SIGNATURE,
    ez(1)) != null

    (stavePositionFinder.getStartBar()..stavePositionFinder.getEndBar()).forEach { bar ->
      if (doIt(option, bar, stavePositionFinder.getStartBar())) {
        val displayBar = if (upBeatBar) bar - 1 else bar

        drawableFactory.numberArea(displayBar, BLOCK_HEIGHT * 2)?.let { numArea ->
          stavePositionFinder.getSlicePosition(ez(bar))?.let { sp ->
            val yPos = copy.getTopForRange(
              sp.start - numArea.width,
              sp.start
            ) - numArea.height - BLOCK_HEIGHT
            copy = copy.addArea(numArea.copy(tag = "BarNumber",
              event = Event(EventType.BAR)), Coord(sp.start - numArea.width, yPos),
              ea(bar).copy(staveId = staveId) )
          }
        }
      }
    }
    return copy
  }

  private fun doIt(param: Any, barNum: Int, startBar: Int): Boolean {
    if (barNum == 1) {
      return false
    }
    return when (param) {
      BarNumbering.EVERY_BAR -> true
      BarNumbering.EVERY_SYSTEM -> barNum == startBar
      is Int -> (barNum - 1) % param == 0
      else -> false
    }
  }
}