package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.NAVIGATION_HEIGHT
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.NavigationType.*

object NavigationDecorator : UpDownDecorator {

  override fun getAreaKey(event: Event): String? {
    return event.getParam<NavigationType>(EventParam.TYPE)?.let { keys[it] }
  }

  override fun getAreaHeight(event: Event) =
    when (event.subType) {
      FINE -> NAVIGATION_HEIGHT / 2
      else -> NAVIGATION_HEIGHT
    }

  override fun getAreaTag(): String = "Navigation"

  override fun isUp(eventAddress: EventAddress, event: Event): Boolean {
    return true
  }

  override fun getXPosition(
    eventAddress: EventAddress, event: Event,
    stavePositionFinder: StavePositionFinder
  ): SlicePosition? {
    return if (isStart(event)) {
      stavePositionFinder.getBarPosition(eventAddress.barNum, false)
        ?.let {
          val pos = it.pos - getAreaHeight(event) / 2
          SlicePosition(pos, pos, 0)
        }
    } else {
      stavePositionFinder.getBarPosition(eventAddress.barNum, true)?.let { barPos ->
        val xPos = barPos.pos + barPos.geog.width - BLOCK_HEIGHT * 4
        SlicePosition(xPos, xPos, 0)
      }
    }
  }

  override fun getEventAddress(eventAddress: EventAddress): EventAddress {
    return eventAddress.staveless()
  }

  private fun isStart(event: Event): Boolean {
    return when (event.subType) {
      DA_CAPO, DAL_SEGNO, FINE -> false
      CODA -> event.isTrue(EventParam.START)
      else -> true
    }
  }
}

private val keys = mapOf(
  CODA to "navigation_coda",
  DAL_SEGNO to "navigation_dal_segno",
  DA_CAPO to "navigation_da_capo",
  FINE to "navigation_fine",
  SEGNO to "navigation_segno"

)
