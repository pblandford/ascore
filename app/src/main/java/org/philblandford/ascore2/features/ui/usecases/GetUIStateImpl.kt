package org.philblandford.ascore2.features.ui.usecases

import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class GetUIStateImpl(private val uiStateRepository: UiStateRepository) : GetUIState {

  override fun invoke(): StateFlow<UIState> {
    return uiStateRepository.getUIState()
  }
}