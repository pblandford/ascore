package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

class GetLyricAtMarkerImpl(private val kScore: KScore) : GetLyricAtMarker {

  override fun invoke(number: Int): String? {
    return kScore.getMarker()?.let { marker ->
      kScore.getEvent(EventType.LYRIC, marker.copy(id = number))?.params?.get(EventParam.TEXT) as? String
    }
  }
}