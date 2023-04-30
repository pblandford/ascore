package org.philblandford.ui.screen.viewmodels

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.drawing.DrawPage
import org.philblandford.ascore2.features.drawing.ScoreChanged
import org.philblandford.ascore2.features.gesture.HandleDrag
import org.philblandford.ascore2.features.gesture.HandleLongPress
import org.philblandford.ascore2.features.gesture.HandleLongPressRelease
import org.philblandford.ascore2.features.gesture.HandleTap
import org.philblandford.ascore2.features.instruments.GetSelectedPart
import org.philblandford.ascore2.features.score.CheckForScore
import org.philblandford.ascore2.features.score.ScoreLoadUpdate
import org.philblandford.ascore2.features.scorelayout.usecases.GetScoreLayout
import org.philblandford.ascore2.features.scorelayout.usecases.ScoreLayout
import org.philblandford.ascore2.features.sound.usecases.GetPlaybackMarker
import org.philblandford.ascore2.features.ui.model.EditItem
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.usecases.GetUIState
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import timber.log.Timber

data class ScreenModel(
  val scoreLayout: ScoreLayout,
  val updateCounter: Int,
  val vertical:Boolean = true,
  val editItem: EditItem? = null
) : VMModel()

interface ScreenInterface : VMInterface {
  fun drawPage(page: Int, drawScope: DrawScope)
  fun handleTap(page: Int, x: Int, y: Int)
  fun handleLongPress(page: Int, x: Int, y: Int)

  fun handleLongPressRelease()
  fun handleDrag(x: Float, y: Float)

  fun toggleMode()
}

sealed class ScreenEffect : VMSideEffect() {
  object Redraw : ScreenEffect()
  data class ScrollToPage(val page:Int) : ScreenEffect()
  object NoScore : ScreenEffect()
}


class ScreenViewModel(
  private val getScoreLayout: GetScoreLayout,
  private val scoreChanged: ScoreChanged,
  private val scoreLoadUpdate: ScoreLoadUpdate,
  private val getPlaybackMarker: GetPlaybackMarker,
  private val getSelectedPart: GetSelectedPart,
  private val drawPageUC: DrawPage,
  private val handleTapUC: HandleTap,
  private val handleLongPressUC: HandleLongPress,
  private val handleLongPressReleaseUC: HandleLongPressRelease,
  private val handleDragUC: HandleDrag,
  getUIState: GetUIState,
  private val checkForScore: CheckForScore
) : BaseViewModel<ScreenModel, ScreenInterface, ScreenEffect>(),
  ScreenInterface {

  init {
    launchEffect(ScreenEffect.Redraw)

    viewModelScope.launch {
      getUIState().collectLatest {
        if (it is UIState.Edit) {
          Timber.e("COORD SVM ${it.editItem.rectangle}")
          update { copy(editItem = it.editItem) }
        } else {
          update { copy(editItem = null) }
        }
      }
    }

    viewModelScope.launch {
      scoreChanged().collect {
        update {
          copy(scoreLayout = getScoreLayout(), updateCounter = updateCounter + 1)
        }
      }
    }
    viewModelScope.launch {
      scoreChanged().map {
        Timber.e("changed ${getSelectedPart().value}")
        getSelectedPart().value
      }.distinctUntilChanged().collectLatest {
        Timber.e("part changed")
        launchEffect(ScreenEffect.ScrollToPage(1))
      }
    }
    viewModelScope.launch {
      scoreLoadUpdate().collectLatest {
        launchEffect(ScreenEffect.ScrollToPage(1))
      }
    }
    viewModelScope.launch {
      getPlaybackMarker().collectLatest { marker ->
        update { copy(updateCounter = updateCounter + 1) }
      }
    }
    viewModelScope.launch {
      getPlaybackMarker().map { it?.page }.distinctUntilChanged().collectLatest { page ->
        page?.let {
          launchEffect(ScreenEffect.ScrollToPage(page))
        }
      }
    }
  }

  override suspend fun initState(): Result<ScreenModel> {
    val layout = getScoreLayout()
    if (layout.numPages == 0) {
      launchEffect(ScreenEffect.NoScore)
    }
    return ScreenModel(layout, 0).ok()
  }

  override fun getInterface() = this

  override fun drawPage(num: Int, drawScope: DrawScope) {
    drawPageUC(num, getPlaybackMarker().value?.eventAddress, drawScope)
  }

  override fun handleTap(page: Int, x: Int, y: Int) {
    handleTapUC(page, x, y)
  }

  override fun handleLongPress(page: Int, x: Int, y: Int) {
    handleLongPressUC(page, x, y)
  }

  override fun handleDrag(x: Float, y: Float) {
    handleDragUC(x, y)
  }

  override fun handleLongPressRelease() {
    handleLongPressReleaseUC()
  }

  override fun toggleMode() {
    update { copy(vertical = !vertical) }
  }
}