package org.philblandford.ascore2.features.page

import com.philblandford.kscore.api.KScore

class GetPageMinMaxImpl(private val kScore: KScore) : GetPageMinMax {
  override fun invoke(): Pair<Int,Int> {
    return kScore.getMinPageWidth() to kScore.getMaxPageWidth()
  }
}