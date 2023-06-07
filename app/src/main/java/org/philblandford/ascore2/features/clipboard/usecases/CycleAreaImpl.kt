package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore

class CycleAreaImpl(private val kScore: KScore) : CycleArea {
  override fun invoke() {
    kScore.cycleArea()
  }
}