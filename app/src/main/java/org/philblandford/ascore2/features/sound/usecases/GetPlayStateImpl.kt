package org.philblandford.ascore2.features.sound.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.log.ksLog
import com.philblandford.kscore.sound.PlayState
import kotlinx.coroutines.flow.StateFlow

class GetPlayStateImpl(private val kScore:KScore) : GetPlayState {
  override operator fun invoke(): StateFlow<PlayState> {
    return kScore.getPlayState()
  }
}