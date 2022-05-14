package org.philblandford.ascore2.features.ui.usecases

import org.philblandford.ascore2.features.ui.model.InsertItem

interface SelectInsertItem {
  operator fun invoke(insertItem:InsertItem)
}