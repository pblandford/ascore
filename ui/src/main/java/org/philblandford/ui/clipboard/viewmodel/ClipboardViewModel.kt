package org.philblandford.ui.clipboard.viewmodel

import org.philblandford.ascore2.features.clipboard.usecases.Copy
import org.philblandford.ascore2.features.clipboard.usecases.Cut
import org.philblandford.ascore2.features.clipboard.usecases.MoveSelection
import org.philblandford.ascore2.features.clipboard.usecases.Paste
import org.philblandford.ascore2.features.edit.MoveSelectedNote
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
  fun selectionLeft()
  fun selectionRight()
  fun copy()
  fun cut()
  fun paste()
}

class ClipboardViewModel(
  private val copyUC: Copy,
  private val cutUC: Cut,
  private val pasteUC: Paste,
  private val moveSelection: MoveSelection,
  private val moveSelectedNote: MoveSelectedNote
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

  override fun noteUp(octave: Boolean) {
    val amount = if (octave) 12 else 1
    moveSelectedNote(amount)
  }

  override fun noteDown(octave: Boolean) {
    val amount = if (octave) -12 else -1
    moveSelectedNote(amount)
  }
}