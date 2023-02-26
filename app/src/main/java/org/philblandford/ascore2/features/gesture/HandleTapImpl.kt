package org.philblandford.ascore2.features.gesture

import com.philblandford.kscore.api.Location
import com.philblandford.kscore.engine.types.Event
import org.philblandford.ascore2.features.clipboard.usecases.ClearSelection
import org.philblandford.ascore2.features.clipboard.usecases.SetEndSelection
import org.philblandford.ascore2.features.insert.InsertEventAtLocation
import org.philblandford.ascore2.features.insert.LocationIsInScore
import org.philblandford.ascore2.features.insert.SetMarker
import org.philblandford.ascore2.features.ui.model.TapInsertBehaviour
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class HandleTapImpl(
  private val uiStateRepository: UiStateRepository,
  private val insertEventAtLocation: InsertEventAtLocation,
  private val setMarker: SetMarker,
  private val locationIsInScore: LocationIsInScore,
  private val setEndSelection: SetEndSelection,
  private val clearSelection: ClearSelection
) : HandleTap {
  override operator fun invoke(page: Int, x: Int, y: Int) {
    val location = Location(page, x, y)
    when (val state = uiStateRepository.getUIState().value) {
      UIState.Input -> setMarker(location)
      is UIState.Insert -> {
        when (uiStateRepository.getInsertItem()?.tapInsertBehaviour) {
          TapInsertBehaviour.INSERT -> insertEventAtLocation(
            location,
            Event(state.insertItem.eventType, state.insertItem.params)
          )
          TapInsertBehaviour.SET_MARKER -> setMarker(location)
          else -> {}
        }
      }
      UIState.Clipboard -> {
        if (locationIsInScore(location)) {
          setEndSelection(location)
        } else {
          clearSelection()
          uiStateRepository.setUiState(UIState.Input)
        }
      }
      UIState.InsertChoose -> {

      }
    }

  }
}