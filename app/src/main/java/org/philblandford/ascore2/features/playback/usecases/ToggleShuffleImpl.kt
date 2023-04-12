package org.philblandford.ascore2.features.playback.usecases

import com.philblandford.kscore.api.KScore

class ToggleShuffleImpl(private val kScore: KScore) : ToggleShuffle {
  override fun invoke() {
    kScore.setShuffleRhythm(!kScore.isShuffleRhythm())
  }
}