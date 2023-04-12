package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.FERMATA_HEIGHT
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventList
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.FermataType.*

object FermataDecorator : UpDownDecorator {

  override fun getAreaKey(event: Event): String? {
    return event.getParam<FermataType>(EventParam.TYPE)?.let { keys[it] } ?: keys[NORMAL]
  }

  override fun getAreaHeight(event: Event) = FERMATA_HEIGHT

  override fun getAreaTag(): String = "Fermata"

  override fun isUp(eventAddress: EventAddress, event: Event): Boolean {
    return true
  }

  override fun getPosWithinSlice(slicePosition: SlicePosition): Int {
    return slicePosition.xMargin - BLOCK_HEIGHT
  }

  override fun getXPosition(
    eventAddress: EventAddress,
    event: Event,
    stavePositionFinder: StavePositionFinder
  ): SlicePosition? {
    return if (stavePositionFinder.getSegmentGeography(eventAddress.copy(offset = dZero())) == null) {
      stavePositionFinder.getBarPosition(eventAddress.barNum)?.let { barPos ->
        val x =
          barPos.pos + barPos.geog.barStartGeography.width + barPos.geog.segmentWidth / 2 - BLOCK_HEIGHT*2
        SlicePosition(x, x, 0)
      }
    } else {
      super.getXPosition(eventAddress, event, stavePositionFinder)
    }
  }

  override fun modifyEvents(eventHash: EventHash, scoreQuery: ScoreQuery): EventList {
    return eventHash.mapNotNull { (k, v) ->
      scoreQuery.getFloorStaveSegment(k.eventAddress)?.let { k.copy(eventAddress = it) to v }
    }.distinct()
  }
}

private val keys = mapOf(
  NORMAL to "fermata_normal",
  SQUARE to "fermata_square",
  TRIANGLE to "fermata_triangle"

)
