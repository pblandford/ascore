package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.KScore

class ZoomInImpl(private val kScore: KScore) : ZoomIn {
  override operator fun invoke() {
    kScore.adjustPageWidth(-100)
  }
}