package org.philblandford.ascore2.features.instruments

import com.philblandford.kscore.api.KScore

class GetVolumeImpl(private val kScore: KScore) : GetVolume {
  override operator fun invoke(part:Int):Int {
    return kScore.getVolume(part)
  }
}