package org.philblandford.ascore2.features.gesture

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location
import org.philblandford.ascore2.features.ui.model.EditItem
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class HandleLongPressImpl(
  private val uiStateRepository: UiStateRepository,
  private val kScore: KScore
) : HandleLongPress {
  override operator fun invoke(page: Int, x: Int, y: Int) {
    uiStateRepository.dragStart()
    val location = Location(page, x, y)
    when (val state = uiStateRepository.getUIState().value) {
      UIState.Input, UIState.Clipboard -> {
        kScore.getEvent(location)?.let { (address, event) ->
          kScore.setStartSelectionOrEvent(location)
          kScore.getSelectedArea()?.let { area ->
            uiStateRepository.setUiState(
              UIState.Edit(
                EditItem(
                  event,
                  address,
                  location.page,
                  area.scoreArea.rectangle
                )
              )
            )
          }
        } ?: run {
          kScore.getEventAddress(location)?.let { eventAddress ->
            uiStateRepository.setUiState(UIState.Clipboard)
            kScore.setStartSelection(eventAddress)
          }
        }
      }
      is UIState.Insert -> {
        if (state.insertItem.rangeCapable) {
          kScore.setStartSelection(location)
        }
      }

      else -> {

      }
    }
  }

}