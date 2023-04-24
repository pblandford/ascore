package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore

class GetKeySignatureAtMarkerImpl(private val kScore: KScore) : GetKeySignatureAtMarker {
  override fun invoke(): Int {
    return kScore.getKeySignatureAtMarker()
  }
}