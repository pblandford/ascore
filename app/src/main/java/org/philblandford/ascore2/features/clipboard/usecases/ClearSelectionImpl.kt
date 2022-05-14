package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class ClearSelectionImpl(private val kScore: KScore,
                         private val uiStateRepository: UiStateRepository) : ClearSelection {
  override operator fun invoke() {
    kScore.clearSelection()
    uiStateRepository.setUiState(UIState.Input)
  }
}