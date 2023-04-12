package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.geographyX.getPadding
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.semiquaver
import com.philblandford.kscore.engine.types.eZero

fun graceArea(segments: Map<Offset, SegmentArea>): Area {

  var area = Area(tag = "Grace")
  segments.forEach { (offset, sa) ->
    val padding = getPadding(semiquaver())
    area = area.addRight(sa.base.copy(tag = "Segment", addressRequirement = AddressRequirement.SEGMENT),
      ignoreMargin = true, eventAddress = eZero().copy(graceOffset = offset))
    area = area.extendRight(padding)
  }
  return area
}
