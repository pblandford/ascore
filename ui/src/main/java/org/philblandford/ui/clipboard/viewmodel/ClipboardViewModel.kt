package org.philblandford.ui.clipboard.viewmodel

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.NoteHeadType
import org.philblandford.ascore2.features.clipboard.usecases.Copy
import org.philblandford.ascore2.features.clipboard.usecases.Cut
import org.philblandford.ascore2.features.clipboard.usecases.DeleteSelection
import org.philblandford.ascore2.features.clipboard.usecases.MoveSelection
import org.philblandford.ascore2.features.clipboard.usecases.Paste
import org.philblandford.ascore2.features.edit.MoveSelectedNote
import org.philblandford.ascore2.features.edit.SetParamForSelected
import org.philblandford.ascore2.features.edit.ToggleBooleanForNotes
import org.philblandford.ascore2.features.insert.InsertTiesAtSelection
import org.philblandford.ascore2.features.insert.RemoveStemSettingsAtSelection
import org.philblandford.ascore2.features.insert.SetStemsAtSelection
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect

data class ClipboardModel(
  val thing:Int = 0
) : VMModel()

interface ClipboardInterface : VMInterface {
  fun noteUp(octave:Boolean)
  fun noteDown(octave: Boolean)
  fun addTies()
  fun selectionLeft()
  fun selectionRight()
  fun copy()
  fun cut()
  fun paste()
  fun delete()
  fun setNoteHead(noteHeadType: NoteHeadType)
  fun toggleSmall()
  fun setStems()
  fun removeStems()
}

class ClipboardViewModel(
  private val copyUC: Copy,
  private val cutUC: Cut,
  private val pasteUC: Paste,
  private val moveSelection: MoveSelection,
  private val moveSelectedNote: MoveSelectedNote,
  private val deleteSelection: DeleteSelection,
  private val setParamForSelected: SetParamForSelected,
  private val toggleBooleanForNotes: ToggleBooleanForNotes,
  private val insertTiesAtSelection: InsertTiesAtSelection,
  private val setStemsAtSelection: SetStemsAtSelection,
  private val removeStemSettingsAtSelection: RemoveStemSettingsAtSelection
  ) : BaseViewModel<ClipboardModel, ClipboardInterface, VMSideEffect>(), ClipboardInterface {

  override suspend fun initState(): Result<ClipboardModel> {
    return ClipboardModel().ok()
  }

  override fun getInterface() = this

  override fun selectionLeft() {
    moveSelection(true)
  }

  override fun selectionRight() {
    moveSelection(false)
  }

  override fun copy() {
    copyUC()
  }

  override fun cut() {
    cutUC()
  }

  override fun paste() {
    pasteUC()
  }

  override fun delete() {
    deleteSelection()
  }

  override fun noteUp(octave: Boolean) {
    val amount = if (octave) 12 else 1
    moveSelectedNote(amount)
  }

  override fun noteDown(octave: Boolean) {
    val amount = if (octave) -12 else -1
    moveSelectedNote(amount)
  }

  override fun setNoteHead(noteHeadType: NoteHeadType) {
    setParamForSelected(EventType.NOTE, EventParam.NOTE_HEAD_TYPE, noteHeadType)
  }

  override fun toggleSmall() {
    toggleBooleanForNotes(EventParam.IS_SMALL)
  }

  override fun addTies() {
    insertTiesAtSelection()
  }

  override fun setStems() {
    setStemsAtSelection()
  }

  override fun removeStems() {
    removeStemSettingsAtSelection()
  }
}