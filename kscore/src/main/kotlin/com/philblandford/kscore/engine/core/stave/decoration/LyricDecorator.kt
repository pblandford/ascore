package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.LyricType
import com.philblandford.kscore.engine.types.StavePositionFinder
import com.philblandford.kscore.option.getOption

object LyricDecorator : Decorator {

  override fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area,
    drawableFactory: DrawableFactory
  ): Area {

    val font = getOption<String>(EventParam.OPTION_LYRIC_FONT, stavePositionFinder.getScoreQuery())
    val size = getOption<Int>(EventParam.OPTION_LYRIC_SIZE, stavePositionFinder.getScoreQuery())
    val yOffset = getOption<List<Pair<Boolean, Int>>>(EventParam.OPTION_LYRIC_OFFSET_BY_POSITION,
      stavePositionFinder.getScoreQuery()).toMap()
    val top = staveArea.getTopForRange(stavePositionFinder.getStartBars(), staveArea.width) + (yOffset[true] ?: 0)
    val bottom = staveArea.getBottomForRange(stavePositionFinder.getStartBars(), staveArea.width) + (yOffset[false] ?: 0)
    val positionMap =
      getOption<List<Pair<Int, Boolean>>>(EventParam.OPTION_LYRIC_POSITIONS, stavePositionFinder.getScoreQuery()).toMap()

    var areaCopy = staveArea
    eventHash.forEach { (k, v) ->
      getArea(v, font, size, drawableFactory)?.let { area ->
        val singleOffset = v.getParam<Coord>(EventParam.HARD_START) ?: Coord()
        val offset = (size * 1.2).toInt() * k.eventAddress.id

        stavePositionFinder.getSlicePosition(k.eventAddress)?.let { sp ->
          val above = v.isAbove(positionMap)
          val xPos = sp.xMargin + singleOffset.x
          val yPos = if (above) top - offset + singleOffset.y
          else bottom + offset + singleOffset.y
          areaCopy = areaCopy.addArea(
            area,
            Coord(xPos, yPos), k.eventAddress
          )
        }
      }
    }
    return areaCopy
  }

  private fun Event.isAbove(positionMap: Map<Int, Boolean>): Boolean {
    val num = getInt(EventParam.NUMBER, 1)
    return positionMap[num] ?: false
  }

  fun getArea(event: Event, font: String, size: Int, drawableFactory: DrawableFactory): Area? {
    return event.getParam<String>(EventParam.TEXT)?.let { text ->
      val type = event.getParam<LyricType>(EventParam.TYPE) ?: LyricType.END
      val fullText = when (type) {
        LyricType.MID -> "$text -"
        LyricType.END -> text
        LyricType.ALL -> text
        LyricType.START -> "$text -"
      }
      drawableFactory.getDrawableArea(TextArgs(fullText, size = size, font = font))?.copy(
        event = event,
        addressRequirement = AddressRequirement.EVENT, tag = "Lyric"
      )
    }
  }

}