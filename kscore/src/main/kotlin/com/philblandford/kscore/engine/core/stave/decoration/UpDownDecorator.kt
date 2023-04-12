package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.representation.DYNAMIC_GAP
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventList
import com.philblandford.kscore.engine.types.*

interface UpDownDecorator : Decorator {

  override fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area,
    drawableFactory: DrawableFactory
  ): Area {
    var areaCopy = staveArea
    val modified = modifyEvents(eventHash, stavePositionFinder.getScoreQuery())
    modified.forEach { (k, v) ->
      drawableFactory.getArea(v)?.let { area ->
        val up = isUp(k.eventAddress, v)
        getXPosition(k.eventAddress, v, stavePositionFinder)?.let {
          val yPos = getYPos(areaCopy, it, area, up)
          val xPos = getPosWithinSlice(it)
          areaCopy = areaCopy.addEventArea(
            v,
            area,
            getEventAddress(k.eventAddress),
            Coord(xPos, yPos)
          )
        }
      }
    }
    return areaCopy
  }

  fun modifyEvents(eventHash: EventHash, scoreQuery: ScoreQuery):EventList {
    return eventHash.toList()
  }

  fun DrawableFactory.getArea(event: Event): Area? {
    return getAreaKey(event)?.let {
      getDrawableArea(ImageArgs(it, INT_WILD, getAreaHeight(event)))?.copy(
        tag = getAreaTag(),
        addressRequirement = AddressRequirement.EVENT, event = event
      )
    }
  }

  fun getXPosition(
    eventAddress: EventAddress,
    event: Event,
    stavePositionFinder: StavePositionFinder
  ): SlicePosition? {
    return stavePositionFinder.getSlicePosition(eventAddress)
  }

  fun getEventAddress(eventAddress: EventAddress): EventAddress = eventAddress

  fun getAreaKey(event: Event): String? = null
  fun getAreaHeight(event: Event): Int = 0
  fun getAreaTag(): String = ""

  fun getYPos(mainArea: Area, slicePosition: SlicePosition, area: Area, up: Boolean): Int {
    return if (up) {
      mainArea.getTopForRange(
        slicePosition.xMargin,
        slicePosition.xMargin + area.width
      ) - area.height - DYNAMIC_GAP
    } else {
      mainArea.getBottomForRange(
        slicePosition.xMargin,
        slicePosition.xMargin + area.width
      ) + DYNAMIC_GAP
    }
  }

  fun align(up:Boolean): Boolean? = up

  fun getPosWithinSlice(slicePosition: SlicePosition) = slicePosition.xMargin


  fun isUp(eventAddress: EventAddress, event: Event): Boolean {
    return eventAddress.id == 0
  }
}
