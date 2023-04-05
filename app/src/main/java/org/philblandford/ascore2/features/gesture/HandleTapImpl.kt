package org.philblandford.ascore2.features.gesture

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location
import com.philblandford.kscore.engine.types.Event
import org.philblandford.ascore2.features.clipboard.usecases.ClearSelection
import org.philblandford.ascore2.features.clipboard.usecases.SetEndSelection
import org.philblandford.ascore2.features.insert.InsertEventAtLocation
import org.philblandford.ascore2.features.insert.LocationIsInScore
import org.philblandford.ascore2.features.insert.SetMarker
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
      UIState.Input -> kScore.setMarker(location)
      is UIState.Insert -> handleInsert(state.insertItem, location)
      UIState.Clipboard -> handleClipboard(location)
      UIState.InsertChoose -> {}
      UIState.Delete -> {
        kScore.deleteEventAt(location)
      }
    }
  }

  private fun handleInsert(insertItem: InsertItem, location:Location) {

    kScore.getStartSelect()?.let { start ->
      if (insertItem.rangeCapable || insertItem.line) {
        kScore.getEventAddress(location)?.let { end ->
          kScore.addEvent(insertItem.eventType, start, insertItem.params, end)
          kScore.clearSelection()
        }
      } else null
    } ?: run {
      kScore.getEventAddress(location)?.let { address ->
        when (insertItem.tapInsertBehaviour) {
          TapInsertBehaviour.INSERT -> {
            if (insertItem.line) {
              kScore.setStartSelection(address)
            } else {
              kScore.addEvent(insertItem.eventType, address, insertItem.params)
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