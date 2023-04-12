package org.philblandford.ui.edit.items.text.viewmodel

import org.philblandford.ascore2.features.edit.MoveSelectedArea
import org.philblandford.ascore2.features.input.usecases.DeleteSelectedEvent
import org.philblandford.ascore2.features.insert.GetFonts
import org.philblandford.ascore2.features.insert.UpdateEvent
import org.philblandford.ascore2.features.ui.usecases.GetUIState
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.edit.viewmodel.EditViewModel

interface TextEditInterface : EditInterface {
  fun getFontStrings():List<String>
  val defaultFont:String
}

class TextEditViewModel(
  getUIState: GetUIState,
  updateEvent: UpdateEvent,
  deleteSelectedEvent: DeleteSelectedEvent,
  moveSelectedArea: MoveSelectedArea,
  private val getFonts: GetFonts
) : EditViewModel(getUIState, updateEvent, deleteSelectedEvent, moveSelectedArea), TextEditInterface {

  override fun getInterface(): EditInterface = this

  override fun getFontStrings(): List<String>  = getFonts()

  override val defaultFont = "default"
}