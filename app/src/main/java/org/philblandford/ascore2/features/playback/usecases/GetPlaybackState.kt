package org.philblandford.ascore2.features.playback.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.playback.entities.PlaybackState

interface GetPlaybackState {
  operator fun invoke(): StateFlow<PlaybackState>
}