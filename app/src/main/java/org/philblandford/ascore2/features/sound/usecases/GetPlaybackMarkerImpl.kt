package org.philblandford.ascore2.features.sound.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.sound.model.PlaybackMarkerInfo

class GetPlaybackMarkerImpl(private val kScore: KScore) : GetPlaybackMarker {

  private val markerInfoFlow = MutableStateFlow<PlaybackMarkerInfo?>(null)
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  init {
    coroutineScope.launch {
      kScore.getPlaybackMarker().collectLatest { marker ->
        val info = marker?.let {
          PlaybackMarkerInfo(marker, kScore.getPage(marker))
        }
        markerInfoFlow.emit(info)
      }
    }
  }

  override fun invoke(): StateFlow<PlaybackMarkerInfo?> {
    return markerInfoFlow
  }
}