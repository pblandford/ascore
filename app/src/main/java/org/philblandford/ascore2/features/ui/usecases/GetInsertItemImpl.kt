package org.philblandford.ascore2.features.ui.usecases

import com.philblandford.kscore.engine.types.EventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class GetInsertItemImpl(private val uiStateRepository: UiStateRepository) :GetInsertItem {
  override operator fun invoke(): Flow<InsertItem?> {
    return uiStateRepository.getUIState().map { state ->
      when (state) {
        is UIState.Insert -> {
          state.insertItem
        }
        else -> null
      }

    }
  }
}