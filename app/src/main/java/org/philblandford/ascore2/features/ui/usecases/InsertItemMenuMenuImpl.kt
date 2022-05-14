package org.philblandford.ascore2.features.ui.usecases

import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class InsertItemMenuMenuImpl(private val uiStateRepository: UiStateRepository) : InsertItemMenu {
  override operator fun invoke() {
    uiStateRepository.setUiState(UIState.InsertChoose)
  }
}