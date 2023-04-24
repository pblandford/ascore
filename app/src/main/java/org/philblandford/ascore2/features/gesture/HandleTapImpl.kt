package org.philblandford.ascore2.features.gesture

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.model.TapInsertBehaviour
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class HandleTapImpl(
  private val uiStateRepository: UiStateRepository,
  private val kScore:KScore
) : HandleTap {
  override operator fun invoke(page: Int, x: Int, y: Int) {

    val location = Location(page, x, y)
    when (val state = uiStateRepository.getUIState().value) {
      UIState.Input, UIState.InsertChoose -> kScore.setMarker(location)
      is UIState.Insert -> handleInsert(state.insertItem, location, uiStateRepository.getVoice().value)
      UIState.Clipboard -> handleClipboard(location)
      UIState.Delete -> {
        kScore.deleteEventAt(location)
      }
      is UIState.Edit -> {
        kScore.clearSelection()
        uiStateRepository.setUiState(UIState.Input)
      }
      is UIState.InsertDelete -> {
        kScore.deleteEventAt(location, state.insertItem.eventType)
      }
      else -> {}
    }
  }

  private fun handleInsert(insertItem: InsertItem, location:Location, voice:Int) {

    kScore.getStartSelect()?.let { start ->
      if (insertItem.isRangeCapable(insertItem.eventType) || insertItem.isLine(insertItem.eventType)) {
        kScore.getEventAddress(location)?.let { end ->
          kScore.addEvent(insertItem.eventType, start.copy(voice = voice), insertItem.params, end.copy(voice = voice))
          kScore.clearSelection()
        }
      } else null
    } ?: run {
      kScore.getEventAddress(location)?.let { address ->
        when (insertItem.tapInsertBehaviour) {
          TapInsertBehaviour.INSERT -> {
            if (insertItem.isLine(insertItem.eventType)) {
              kScore.setStartSelection(address)
            } else {
              kScore.addEvent(insertItem.eventType, address.copy(voice = voice), insertItem.params)
            }
          }
          TapInsertBehaviour.SET_MARKER -> kScore.setMarker(location)
          else -> {}
        }
      }
    }
  }

  private fun handleClipboard(location: Location) {
    kScore.getEventAddress(location)?.let { address ->
      kScore.setEndSelection(address)
    } ?: run {
      kScore.clearSelection()
      uiStateRepository.setUiState(UIState.Input)
    }
  }
}