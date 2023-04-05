package org.philblandford.ascore2.features.gesture

import org.philblandford.ascore2.features.input.usecases.Delete
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class HandleDeletePressImpl(private val uiStateRepository: UiStateRepository,
private val delete:Delete) : HandleDeletePress {

  override fun invoke() {
    when (uiStateRepository.getUIState().value) {
      UIState.Delete -> uiStateRepository.setUiState(UIState.Input)
      else -> delete()
    }
  }
}