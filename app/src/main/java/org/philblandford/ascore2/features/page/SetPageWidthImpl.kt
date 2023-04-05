package org.philblandford.ascore2.features.page

import com.philblandford.kscore.api.KScore

class SetPageWidthImpl(private val kScore: KScore) : SetPageWidth {
  override fun invoke(width: Int) {
    kScore.setPageWidth(width)
  }
}