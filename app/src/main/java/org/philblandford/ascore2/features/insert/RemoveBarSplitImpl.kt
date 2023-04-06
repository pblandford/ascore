package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore

class RemoveBarSplitImpl(private val kScore: KScore) : RemoveBarSplit {

  override fun invoke() {
    kScore.removeBarSplit()
  }
}