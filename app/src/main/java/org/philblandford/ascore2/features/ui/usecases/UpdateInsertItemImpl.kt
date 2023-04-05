package org.philblandford.ascore2.features.ui.usecases

import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class UpdateInsertItemImpl(private val uiStateRepository: UiStateRepository) : UpdateInsertItem {
  override fun invoke(func: InsertItem.() -> InsertItem) {
    uiStateRepository.getInsertItem()?.let { item ->
      uiStateRepository.setInsertItem(item.func())
    }
  }
}