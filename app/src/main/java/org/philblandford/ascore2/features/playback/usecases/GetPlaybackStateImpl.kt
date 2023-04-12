package org.philblandford.ascore2.features.playback.usecases

import com.philblandford.kscore.api.KScore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.philblandford.ascore2.features.playback.entities.PlaybackState

class GetPlaybackStateImpl(private val kScore: KScore) : GetPlaybackState {

  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  override fun invoke(): StateFlow<PlaybackState> {
    return kScore.scoreUpdate().map {
      stateFromScore()
    }.stateIn(coroutineScope, SharingStarted.Eagerly, stateFromScore())
  }

  private fun stateFromScore() =
    PlaybackState(kScore.isShuffleRhythm(), kScore.isHarmonyPlayback(), kScore.isLoop())
}