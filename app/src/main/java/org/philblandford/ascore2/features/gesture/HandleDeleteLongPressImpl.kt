package org.philblandford.ascore2.features.gesture

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventType
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class HandleDeleteLongPressImpl(private val uiStateRepository: UiStateRepository,
private val kScore: KScore) : HandleDeleteLongPress {
  override fun invoke() {
    when(uiStateRepository.getUIState().value) {
      UIState.Delete -> uiStateRepository.setUiState(UIState.Input)
      UIState.Clipboard -> {
        kScore.deleteRange(EventType.BAR)
        kScore.clearSelection()
        uiStateRepository.setUiState(UIState.Input)
      }
      else -> uiStateRepository.setUiState(UIState.Delete)
    }
  }
}