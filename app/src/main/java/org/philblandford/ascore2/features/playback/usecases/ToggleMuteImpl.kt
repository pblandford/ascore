package org.philblandford.ascore2.features.playback.usecases

import com.philblandford.kscore.api.KScore

class ToggleMuteImpl(private val kScore: KScore) : ToggleMute {

  override fun invoke(part: Int) {
    val muted = kScore.isMute(part)
    kScore.setMute(part, !muted)
  }
}