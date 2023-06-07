package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventType
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class DeleteSelectionImpl(private val kScore: KScore,
private val uiStateRepository: UiStateRepository) : DeleteSelection {
  override fun invoke() {
    kScore.getSelectedArea()?.let {
      kScore.deleteSelectedEvent()
    } ?: run {
      kScore.deleteRange()
      kScore.clearSelection()
      uiStateRepository.setUiState(UIState.Input)
    }
  }
}