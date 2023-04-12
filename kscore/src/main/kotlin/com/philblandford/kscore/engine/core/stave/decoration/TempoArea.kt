package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DotArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.DOT_WIDTH
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.INT_WILD


fun DrawableFactory.tempoArea(event: Event):Area? {
  if (event.isTrue(EventParam.HIDDEN)) {
    return null
  }
  return getSymbolArea(event)?.let { symbol ->
    getTextArea(event)?.let {text ->
      var area = Area(tag = "Tempo", event = event, addressRequirement = AddressRequirement.EVENT)
      area = area.addArea(symbol)
      getDotArea(event)?.let { area = area.addRight(it, gap = DOT_WIDTH, y = area.height - DOT_WIDTH) }
      area = area.addRight(text, y = symbol.height - text.height)
      area
    }
  }
}

private fun DrawableFactory.getSymbolArea(event: Event): Area? {
  return event.getParam<Duration>(EventParam.DURATION)?.let { duration ->
    durationKeys[duration.undot()]?.let {
      getDrawableArea(ImageArgs(it, INT_WILD, BLOCK_HEIGHT*4))
    }
  }
}

private fun DrawableFactory.getDotArea(event:Event): Area? {
  return if (event.getParam<Duration>(EventParam.DURATION)?.numDots() == 1) {
    getDrawableArea(DotArgs(DOT_WIDTH, DOT_WIDTH))
  } else null
}

private val durationKeys = mapOf(
  hemidemisemiquaver() to "note_hemidemisemiquaver",
  demisemiquaver() to "note_demisemiquaver",
  semiquaver() to "note_semiquaver",
  quaver() to "note_quaver",
  crotchet() to "note_crotchet",
  minim() to "note_minim",
  semibreve() to "note_semibreve",
  breve() to "note_breve",
  longa() to "note_longa"
)

private fun DrawableFactory.getTextArea(event: Event): Area? {
  return event.getParam<Int>(EventParam.BPM)?.let { bpm ->
    getDrawableArea(TextArgs(" = ${bpm}"))
  }
}