package org.philblandford.ascore2.features.sound.usecases

import com.philblandford.kscore.api.KScore

class PlayImpl(
  private val kScore: KScore,
) : Play {

  override operator fun invoke() {
    kScore.play()
  }
}