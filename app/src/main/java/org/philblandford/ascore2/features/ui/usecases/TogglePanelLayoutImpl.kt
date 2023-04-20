package org.philblandford.ascore2.features.ui.usecases

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class TogglePanelLayoutImpl(private val uiStateRepository: UiStateRepository,
val kScore: KScore) : TogglePanelLayout {
  override operator fun invoke() {
    val current = uiStateRepository.getUIState().value
    val state = when (current) {
      UIState.Input -> UIState.InsertChoose
      else -> UIState.Input
    }

    uiStateRepository.setUiState(state)
    kScore.clearSelection()
  }
}