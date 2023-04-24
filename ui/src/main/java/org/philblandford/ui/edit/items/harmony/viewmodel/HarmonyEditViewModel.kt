package org.philblandford.ui.edit.items.harmony.viewmodel

import com.philblandford.kscore.engine.pitch.qualityNames
import com.philblandford.kscore.engine.types.Pitch
import org.philblandford.ascore2.features.edit.MoveSelectedArea
import org.philblandford.ascore2.features.input.usecases.DeleteSelectedEvent
import org.philblandford.ascore2.features.insert.GetDefaultTextSize
import org.philblandford.ascore2.features.insert.InsertEvent
import org.philblandford.ascore2.features.insert.UpdateEventParam
import org.philblandford.ascore2.features.settings.usecases.GetAvailableFonts
import org.philblandford.ascore2.features.ui.usecases.GetUIState
import org.philblandford.ui.edit.items.text.viewmodel.TextEditInterface
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.edit.viewmodel.EditViewModel

interface HarmonyEditInterface : EditInterface {
  fun getNotes():List<Pitch>
  fun getQualities():List<String>
}

class HarmonyEditViewModel(
  getUIState: GetUIState,
  updateEvent: UpdateEventParam,
  insertEvent: InsertEvent,
  deleteSelectedEvent: DeleteSelectedEvent,
  moveSelectedArea: MoveSelectedArea,

) : EditViewModel(getUIState, updateEvent, insertEvent, deleteSelectedEvent, moveSelectedArea),
  HarmonyEditInterface {

  override fun getNotes(): List<Pitch> {
    return Pitch.allPitches
  }

  override fun getQualities(): List<String> {
    return qualityNames
  }
}