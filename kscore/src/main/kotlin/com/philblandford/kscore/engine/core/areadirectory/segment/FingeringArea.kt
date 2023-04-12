package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.header.numberArea
import com.philblandford.kscore.engine.core.representation.ARTICULATION_GAP
import com.philblandford.kscore.engine.core.representation.FINGERING_HEIGHT
import com.philblandford.kscore.engine.core.representation.FINGERING_OFFSET
import com.philblandford.kscore.engine.newadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.paramMapOf

fun DrawableFactory.fingeringArea(fingering: ChordDecoration<Int>): Area? {
  var area = Area(
    tag = "Fingering", event = Event(
      EventType.FINGERING,
      params = paramMapOf(EventParam.FINGERING to fingering.items)
    ),
    addressRequirement = AddressRequirement.EVENT
  )

  fingering.items.sorted().forEach { num ->
    val gap = if (num == fingering.items.first()) 0 else ARTICULATION_GAP
    numberArea(num, FINGERING_HEIGHT)?.let {
      area = area.addBelow(it, gap, x = FINGERING_OFFSET)
    }
  }
  return area
}