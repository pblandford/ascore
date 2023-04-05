package org.philblandford.ascore2.features.page

import com.philblandford.kscore.api.KScore

class GetSegmentWidthImpl(private val kScore: KScore) : GetSegmentWidth {
  override fun invoke(): Int {
    return kScore.getMarker()?.let { kScore.getSegmentArea(it)?.width }
      ?: kScore.getMinSegmentWidth()
  }
}