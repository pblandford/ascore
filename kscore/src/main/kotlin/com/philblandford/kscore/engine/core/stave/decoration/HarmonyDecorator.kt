package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.cZero
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.pitch.harmony
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.StavePositionFinder
import com.philblandford.kscore.option.getOption

object HarmonyDecorator : Decorator {

  override fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area, drawableFactory: DrawableFactory
  ): Area {

    val offset =
      getOption<Coord>(EventParam.OPTION_HARMONY_OFFSET, stavePositionFinder.getScoreQuery()) ?: cZero()
    val top = staveArea.getTopForRange(stavePositionFinder.getStartBars(), staveArea.width) + offset.y
    val font =
      getOption<String>(EventParam.OPTION_HARMONY_FONT, stavePositionFinder.getScoreQuery())
    val size = getOption<Int>(EventParam.OPTION_HARMONY_SIZE, stavePositionFinder.getScoreQuery())
    var areaCopy = staveArea
    eventHash.forEach { (k, v) ->
      getArea(v, font, size, drawableFactory)?.let { area ->
        val singleOffset = v.getParam<Coord>(EventParam.HARD_START) ?: cZero()
        stavePositionFinder.getSlicePosition(k.eventAddress)?.let { sp ->
          areaCopy = areaCopy.addEventArea(
            v,
            area,
            k.eventAddress,
            Coord(sp.xMargin + singleOffset.x, top - area.height - BLOCK_HEIGHT + singleOffset.y),
            ignoreHardStart = true
          )
        }
      }
    }
    return areaCopy
  }

  fun getArea(event: Event, font: String, size: Int, drawableFactory: DrawableFactory): Area? {
    return harmony(event)?.let {
      drawableFactory.getDrawableArea(TextArgs(it.toString(), size = size, font = font))?.copy(
        event = event,
        addressRequirement = AddressRequirement.EVENT,
        tag = "Harmony"
      )
    }
  }

}