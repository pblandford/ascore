package org.philblandford.ascore2.features.gesture

import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class HandleDeleteLongPressImpl(private val uiStateRepository: UiStateRepository) : HandleDeleteLongPress {
  override fun invoke() {
    when(uiStateRepository.getUIState().value) {
      UIState.Delete -> uiStateRepository.setUiState(UIState.Input)
      else -> uiStateRepository.setUiState(UIState.Delete)
    }
  }
}