package org.philblandford.ascore2.features.page

import com.philblandford.kscore.api.KScore

class GetPageWidthImpl(private val kScore: KScore) : GetPageWidth {
  override fun invoke(): Int {
    return kScore.getPageWidth()
  }
}