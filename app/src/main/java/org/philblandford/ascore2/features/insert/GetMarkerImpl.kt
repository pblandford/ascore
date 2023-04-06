package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventAddress

class GetMarkerImpl(private val kScore: KScore) : GetMarker {

  override fun invoke(): EventAddress? {
    return kScore.getMarker()
  }
}