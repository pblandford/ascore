package org.philblandford.ascore2.features.playback.usecases

import com.philblandford.kscore.api.KScore

class ToggleSoloImpl(private val kScore: KScore) : ToggleSolo {
  override fun invoke(part: Int) {
    kScore.setSolo(part, !kScore.isSolo(part))
  }
}