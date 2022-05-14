package org.philblandford.ascore2.features.sound.usecases

import com.philblandford.kscore.engine.types.EventAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.sound.model.PlaybackMarkerInfo

interface GetPlaybackMarker {
  operator fun invoke(): StateFlow<PlaybackMarkerInfo?>
}