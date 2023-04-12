package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.core.representation.FERMATA_HEIGHT
import com.philblandford.kscore.engine.core.representation.PAUSE_WIDTH
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.PauseType
import com.philblandford.kscore.engine.types.PauseType.BREATH
import com.philblandford.kscore.engine.types.PauseType.CAESURA

object PauseDecorator : UpDownDecorator {

  override fun getAreaKey(event: Event): String? {
    return event.getParam<PauseType>(EventParam.TYPE)?.let { keys[it] }
  }

  override fun getAreaHeight(event: Event) = FERMATA_HEIGHT

  override fun getAreaTag() = "Pause"

  override fun isUp(eventAddress: EventAddress, event: Event): Boolean {
    return true
  }

  override fun getPosWithinSlice(slicePosition: SlicePosition): Int {
    return slicePosition.start + slicePosition.width - PAUSE_WIDTH/2
  }
}

private val keys = mapOf(
  BREATH to "pause_breath",
  CAESURA to "pause_caesura"
)
