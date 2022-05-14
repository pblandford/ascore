package org.philblandford.ascore2.features.sound.usecases

import com.philblandford.kscore.sound.PlayState
import kotlinx.coroutines.flow.StateFlow

interface GetPlayState {
  operator fun invoke():StateFlow<PlayState>
}