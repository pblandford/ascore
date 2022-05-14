package org.philblandford.ui.main.toprow

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.sound.PlayState
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.sound.usecases.GetPlayState
import org.philblandford.ascore2.features.sound.usecases.Pause
import org.philblandford.ascore2.features.sound.usecases.Play
import org.philblandford.ascore2.features.sound.usecases.Stop
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect

data class PlayModel(
  val playState: PlayState
) : VMModel()

interface PlayInterface : VMInterface {
  fun togglePlay()
  fun pause()
}

class PlayViewModel(
  private val play: Play,
  private val stop: Stop,
  private val pauseUC:Pause,
  private val getPlayState: GetPlayState
) : BaseViewModel<PlayModel, PlayInterface, VMSideEffect>(), PlayInterface {

  init {
    viewModelScope.launch {
      getPlayState().collect{ state ->
        update { copy(playState = state) }
      }
    }
  }

  override suspend fun initState(): Result<PlayModel> {
    return PlayModel(PlayState.STOPPED).ok()
  }

  override fun getInterface() = this

  override fun togglePlay() {
    if (getState().value?.playState != PlayState.PLAYING) {
      play()
    } else {
      stop()
    }
  }

  override fun pause() {
    pauseUC()
  }
}
