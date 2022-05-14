package org.philblandford.ascore2.features.instruments

import com.philblandford.kscore.api.KScore

class SetVolumeImpl(private val kScore: KScore) : SetVolume {
  override operator fun invoke(part:Int, volume: Int) {
    kScore.setVolume(part, volume)
  }
}