package org.philblandford.ascore2.features.page

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

class GetSegmentWidthImpl(private val kScore: KScore) : GetSegmentWidth {
  override fun invoke(): Int {
    return (kScore.getMarker()?.let { kScore.getParam<Int>(EventType.SPACE, EventParam.AMOUNT, it) }
      ?: 0) + kScore.getMinSegmentWidth()
  }
}