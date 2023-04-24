package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class DeleteSelectionImpl(private val kScore: KScore,
private val uiStateRepository: UiStateRepository) : DeleteSelection {
  override fun invoke() {
    kScore.deleteRange()
    kScore.clearSelection()
    uiStateRepository.setUiState(UIState.Input)
  }
}