package org.philblandford.ascore2.features.ui.model

import com.philblandford.kscore.api.Location


sealed class UIState {
  object Input : UIState()
  object InsertChoose : UIState()
  data class Insert(val insertItem: InsertItem) : UIState()

  data class InsertDelete(val insertItem: InsertItem) : UIState()
  data class Edit(val editItem: EditItem) : UIState()
  object Clipboard : UIState()

  data class MoveNote(val location:Location) : UIState()
  object Delete : UIState()
}