package org.philblandford.ascore2.features.sound.usecases

import com.philblandford.kscore.api.KScore

class PauseImpl(private val kScore: KScore) : Pause {
  override operator fun invoke() {
    kScore.pause()
  }
}