package org.philblandford.ui.edit.items.text.viewmodel

import org.philblandford.ascore2.features.edit.MoveSelectedArea
import org.philblandford.ascore2.features.input.usecases.DeleteSelectedEvent
import org.philblandford.ascore2.features.insert.GetDefaultTextSize
import org.philblandford.ascore2.features.insert.InsertEvent
import org.philblandford.ascore2.features.insert.UpdateEventParam
import org.philblandford.ascore2.features.settings.usecases.GetAvailableFonts
import org.philblandford.ascore2.features.ui.usecases.GetUIState
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.edit.viewmodel.EditViewModel

interface TextEditInterface : EditInterface {
  fun getFontStrings():List<String>
  val defaultFont:String
  fun defaultTextSize():Int
}

class TextEditViewModel(
  getUIState: GetUIState,
  updateEvent: UpdateEventParam,
  insertEvent: InsertEvent,
  deleteSelectedEvent: DeleteSelectedEvent,
  moveSelectedArea: MoveSelectedArea,
  private val getFonts: GetAvailableFonts,
  private val getDefaultTextSize: GetDefaultTextSize
) : EditViewModel(getUIState, updateEvent, insertEvent, deleteSelectedEvent, moveSelectedArea), TextEditInterface {

  override fun getInterface(): EditInterface = this

  override fun getFontStrings(): List<String>  = getFonts()

  override val defaultFont = "default"

  override fun defaultTextSize(): Int {
    return getState().value?.editItem?.let { item ->
      getDefaultTextSize(item.event.eventType)
    } ?: 100
  }
}