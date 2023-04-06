package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class GetLyricAtMarkerImpl(private val kScore: KScore, private val uiStateRepository: UiStateRepository) : GetLyricAtMarker {

  override fun invoke(number: Int): String? {
    return kScore.getMarker()?.let { marker ->
      val voice = uiStateRepository.getVoice().value
      kScore.getEvent(EventType.LYRIC, marker.copy(voice = voice, id = number))?.params?.get(EventParam.TEXT) as? String
    }
  }
}