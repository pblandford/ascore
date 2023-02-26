package org.philblandford.ascore2.features.ui.usecases

import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class UpdateInsertEventImpl(private val uiStateRepository: UiStateRepository) : UpdateInsertEvent {
  override operator fun invoke(eventType: EventType) {
    (uiStateRepository.getUIState().value as? UIState.Insert)?.let { state ->
      uiStateRepository.setInsertItem(state.insertItem.copy(
        eventType = eventType))
    }
  }
}