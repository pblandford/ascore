package org.philblandford.ascore2.features.gesture

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location
import com.philblandford.kscore.engine.types.DurationType
import com.philblandford.kscore.engine.types.EventType
import org.philblandford.ascore2.features.ui.model.EditItem
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository
import timber.log.Timber

class HandleLongPressImpl(
  private val uiStateRepository: UiStateRepository,
  private val kScore: KScore
) : HandleLongPress {
  override operator fun invoke(page: Int, x: Int, y: Int) {
    uiStateRepository.dragStart()
    val location = Location(page, x, y)
    when (val state = uiStateRepository.getUIState().value) {
      UIState.Input, UIState.Clipboard, UIState.InsertChoose, is UIState.Edit -> {
        kScore.getEvent(location)?.let { (address, event) ->
          Timber.e("TS got event $address $event")
          kScore.setStartSelectionOrEvent(location)
          kScore.getSelectedArea()?.let { area ->
            Timber.e("TS got selected $area")
            uiStateRepository.setUiState(
              UIState.Edit(
                EditItem(
                  area.event,
                  area.eventAddress,
                  location.page,
                  area.scoreArea.rectangle
                )
              )
            )
          }
        } ?: run {
          kScore.getEventAddress(location)?.let { eventAddress ->
            if (kScore.getEvent(
                EventType.DURATION,
                eventAddress.copy(voice = uiStateRepository.getVoice().value)
              )?.subType == DurationType.CHORD
            ) {
              uiStateRepository.setUiState(UIState.MoveNote(location))
              kScore.setStartSelection(eventAddress)
            } else {
              kScore.setStartSelection(eventAddress)
              uiStateRepository.setUiState(UIState.Clipboard)
            }
          }
        }
      }

      is UIState.Insert -> {
        if (state.insertItem.isRangeCapable(state.insertItem.eventType)) {
          kScore.setStartSelection(location)
        }
      }

      is UIState.InsertDelete -> {
        if (state.insertItem.isRangeCapable(state.insertItem.eventType)) {
          kScore.setStartSelection(location)
        }
      }

      else -> {

      }
    }
  }

}