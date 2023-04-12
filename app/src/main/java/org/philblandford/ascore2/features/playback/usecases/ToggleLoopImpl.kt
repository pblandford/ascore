package org.philblandford.ascore2.features.playback.usecases

import com.philblandford.kscore.api.KScore

class ToggleLoopImpl(private val kScore: KScore) : ToggleLoop {
  override fun invoke() {
    kScore.setLoop(!kScore.isLoop())
  }
}