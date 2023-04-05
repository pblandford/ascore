package org.philblandford.ascore2.features.page

import com.philblandford.kscore.api.KScore

class GetSegmentMinMaxImpl(private val kScore: KScore) : GetSegmentMinMax {
  override fun invoke(): Pair<Int, Int> {
    return kScore.getMinSegmentWidth() to kScore.getMaxSegmentWidth()
  }
}