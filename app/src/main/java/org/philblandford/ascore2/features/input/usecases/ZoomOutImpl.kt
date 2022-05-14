package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.KScore

class ZoomOutImpl(private val kScore: KScore) : ZoomOut {
  override operator fun invoke() {
    kScore.adjustPageWidth(-100)
  }
}