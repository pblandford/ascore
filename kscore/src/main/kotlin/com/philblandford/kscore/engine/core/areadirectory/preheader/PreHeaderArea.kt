package com.philblandford.kscore.engine.core.areadirectory.preheader

import com.philblandford.kscore.engine.core.PreHeaderGeography
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.representation.PREHEADER_GAP
import com.philblandford.kscore.engine.core.representation.STAVE_JOIN_THICKNESS
import com.philblandford.kscore.engine.core.representation.TEXT_SIZE
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

data class PreHeaderArea(val base: Area, val preHeaderGeography: PreHeaderGeography) {}

enum class LabelType {
  NONE, SHORT, FULL
}

fun DrawableFactory.preHeaderArea(
  map: Map<EventType, Event>,
  eventAddress: EventAddress,
  labelType: LabelType
): PreHeaderArea? {

  var area = Area(tag = "PreHeader")
  var geog = PreHeaderGeography(0, 0)

  if (labelType != LabelType.NONE) {
    val param = if (labelType == LabelType.FULL) EventParam.LABEL else EventParam.ABBREVIATION

    map[EventType.PART]?.let { event ->

      event.getParam<String>(param)?.let { name ->
        val textArgs = TextArgs(
          name, size = event.getParam<Int>(EventParam.TEXT_SIZE) ?: TEXT_SIZE,
          font = event.getParam<String>(EventParam.FONT)
        )

        getDrawableArea(textArgs)?.let {
          area = area.addArea(
            it.copy(tag = "PartName", event = event, addressRequirement = AddressRequirement.EVENT)
              .extendRight(
                PREHEADER_GAP
              ),
            eventAddress = eventAddress
          )
          geog = geog.copy(textWidth = area.width)
        }
      }
    }
  }

  map[EventType.STAVE_JOIN]?.let {
    area = area.extendRight(STAVE_JOIN_THICKNESS)
    geog = geog.copy(joinWidth = STAVE_JOIN_THICKNESS)
  }

  return PreHeaderArea(area, geog)
}