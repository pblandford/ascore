package org.philblandford.ascore2.features.sound.usecases

import com.philblandford.kscore.api.KScore

class StopImpl(private val kScore: KScore) : Stop {
  
  override operator fun invoke() {
      kScore.stop()
  }
}