package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class DeleteSelectedEventImpl(private val kScore: KScore,
                              private val uiStateRepository: UiStateRepository) : DeleteSelectedEvent {
  override fun invoke() {
    kScore.deleteSelectedEvent()
    kScore.clearSelection()
    uiStateRepository.setUiState(UIState.Input)
  }
}