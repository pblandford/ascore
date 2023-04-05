package org.philblandford.ascore2.features.page

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

class SetSegmentWidthImpl(private val kScore: KScore) : SetSegmentWidth {
  override fun invoke(width: Int) {
    if (width == -1) {
      kScore.deleteEventAtMarker(EventType.SPACE)
    } else {
      kScore.setParamAtMarker(EventType.SPACE, EventParam.AMOUNT, width)
    }
  }
}