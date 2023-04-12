package org.philblandford.ascore2.features.playback.usecases

import com.philblandford.kscore.api.KScore

class ToggleHarmoniesImpl(private val kScore: KScore) : ToggleHarmonies {
  override fun invoke() {
    kScore.setHarmonyPlayback(!kScore.isHarmonyPlayback())
  }
}