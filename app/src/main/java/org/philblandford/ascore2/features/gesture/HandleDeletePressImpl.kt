package org.philblandford.ascore2.features.gesture

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import org.philblandford.ascore2.features.clipboard.usecases.ClearSelection
import org.philblandford.ascore2.features.input.usecases.Delete
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class HandleDeletePressImpl(
  private val uiStateRepository: UiStateRepository,
  private val clearSelection: ClearSelection,
  private val kScore: KScore
) : HandleDeletePress {

  override fun invoke() {
    when (val state = uiStateRepository.getUIState().value) {
      UIState.Delete -> uiStateRepository.setUiState(UIState.Input)
      is UIState.Insert -> {
        when (state.insertItem.eventType) {
          EventType.HARMONY -> {
            kScore.deleteEventAtMarker(state.insertItem.eventType)
            kScore.moveMarker()
          }
          EventType.LYRIC -> {
            val voice = uiStateRepository.getVoice().value
            val id = uiStateRepository.getInsertItem()?.getParam<Int>(EventParam.NUMBER) ?: 1
            kScore.deleteEventAtMarker(EventType.LYRIC, voice, id)
            kScore.moveMarker()
          }
          else -> {}
        }
      }
      is UIState.Clipboard -> {
        kScore.getStartSelect()?.let {
          kScore.deleteRange()
          clearSelection()
        }
      }
      else -> kScore.deleteAtMarker(uiStateRepository.getVoice().value)
    }
  }
}