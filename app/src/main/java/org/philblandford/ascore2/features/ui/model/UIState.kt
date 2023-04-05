package org.philblandford.ascore2.features.ui.model



sealed class UIState {
  object Input : UIState()
  object InsertChoose : UIState()
  data class Insert(val insertItem: InsertItem) : UIState()
  object Clipboard : UIState()
  object Delete : UIState()
}