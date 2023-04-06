package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore

class SplitBarImpl(private val kScore: KScore) : SplitBar {
  override fun invoke() {
    kScore.splitBar()
  }
}