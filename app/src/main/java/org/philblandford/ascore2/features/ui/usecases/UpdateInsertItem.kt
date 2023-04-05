package org.philblandford.ascore2.features.ui.usecases

import org.philblandford.ascore2.features.ui.model.InsertItem

interface UpdateInsertItem {
  operator fun invoke(func:InsertItem.()->InsertItem)
}