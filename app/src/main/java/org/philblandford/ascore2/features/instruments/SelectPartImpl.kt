package org.philblandford.ascore2.features.instruments

import com.philblandford.kscore.api.KScore

class SelectPartImpl(private val kScore: KScore) : SelectPart {
  override operator fun invoke(part:Int) {
    kScore.setSelectedPart(part)
  }
}