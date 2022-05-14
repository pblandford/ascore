package org.philblandford.ascore2.features.gesture

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class HandleTapImpl(
  private val uiStateRepository: UiStateRepository,
  private val kScore: KScore
) : HandleTap {
  override operator fun invoke(page: Int, x: Int, y: Int) {
    val location = Location(page, x, y)
    when (val state = uiStateRepository.getUIState().value) {
      UIState.Input -> kScore.setMarker(location)
      is UIState.Insert -> {
          kScore.getEventAddress(location)?.let { eventAddress ->
            kScore.addEvent(state.insertItem.eventType, eventAddress, state.insertItem.params)
        }
      }
      UIState.Clipboard -> {
        kScore.getEventAddress(location)?.let { eventAddress ->
          kScore.setEndSelection(eventAddress)
        } ?: run {
          kScore.clearSelection()
          uiStateRepository.setUiState(UIState.Input)
        }
      }
    }

  }
}