package org.philblandford.ascore2.features.ui.usecases

import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class SelectInsertItemImpl(private val uiStateRepository: UiStateRepository)  : SelectInsertItem{
  override operator fun invoke(insertItem:InsertItem) {
    uiStateRepository.setUiState(UIState.Insert(insertItem))
  }
}