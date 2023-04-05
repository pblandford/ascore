package org.philblandford.ui.screen.viewmodels

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.drawing.DrawPage
import org.philblandford.ascore2.features.drawing.ScoreChanged
import org.philblandford.ascore2.features.gesture.HandleLongPress
import org.philblandford.ascore2.features.gesture.HandleTap
import org.philblandford.ascore2.features.score.CreateDefaultScore
import org.philblandford.ascore2.features.scorelayout.usecases.GetScoreLayout
import org.philblandford.ascore2.features.scorelayout.usecases.ScoreLayout
import org.philblandford.ascore2.features.sound.usecases.GetPlaybackMarker
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect

data class ScreenZoomModel(
  val scoreLayout: ScoreLayout,
  val updateCounter: Int,
) : VMModel()

interface ScreenZoomInterface : VMInterface {
  fun drawPage(page: Int, drawScope: DrawScope)
}

sealed class ScreenZoomEffect : VMSideEffect() {
  object Redraw : ScreenEffect()
}


class ScreenZoomViewModel(
  private val getScoreLayout: GetScoreLayout,
  private val scoreChanged: ScoreChanged,
  private val getPlaybackMarker: GetPlaybackMarker,
  private val drawPageUC: DrawPage,
) : BaseViewModel<ScreenZoomModel, ScreenZoomInterface, ScreenEffect>(),
  ScreenZoomInterface {

  init {
    launchEffect(ScreenEffect.Redraw)

    viewModelScope.launch {
      scoreChanged().collect {
        update {
          copy(scoreLayout = getScoreLayout(), updateCounter = updateCounter + 1)
        }
      }
    }
    viewModelScope.launch {
      getPlaybackMarker().collectLatest { marker ->
        update { copy(updateCounter = updateCounter + 1) }
      }
    }
  }

  override suspend fun initState(): Result<ScreenZoomModel> {
    return ScreenZoomModel(getScoreLayout(), 0).ok()
  }

  override fun getInterface() = this

  override fun drawPage(num: Int, drawScope: DrawScope) {

    drawPageUC(num, getPlaybackMarker().value?.eventAddress, drawScope)
  }

}