package org.philblandford.ascore2.features.gesture

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class HandleLongPressImpl(
  private val uiStateRepository: UiStateRepository,
  private val kScore: KScore
) : HandleLongPress {
  override operator fun invoke(page: Int, x: Int, y: Int) {
    val location = Location(page, x, y)
    when (uiStateRepository.getUIState().value) {
      UIState.Input, UIState.Clipboard -> {
        kScore.getEventAddress(location)?.let { eventAddress ->
          uiStateRepository.setUiState(UIState.Clipboard)
          kScore.setStartSelection(eventAddress)
        }
      }

      else -> {

      }
    }
  }
}