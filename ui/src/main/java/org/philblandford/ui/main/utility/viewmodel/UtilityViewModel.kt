package org.philblandford.ui.main.utility.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.clipboard.usecases.ClearSelection
import org.philblandford.ascore2.features.gesture.HandleDeleteLongPress
import org.philblandford.ascore2.features.gesture.HandleDeletePress
import org.philblandford.ascore2.features.input.usecases.*
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.usecases.GetPanelLayout
import org.philblandford.ascore2.features.ui.usecases.GetUIState
import org.philblandford.ascore2.features.ui.usecases.TogglePanelLayout
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect


data class UtilityModel(
  val deleteSelected: Boolean,
  val voice: Int,
  val showPanel: Boolean,
  val panelType: LayoutID
) : VMModel()


interface UtilityInterface : VMInterface {
  fun delete()
  fun deleteLong()
  fun toggleVoice()
  fun zoomIn()
  fun zoomOut()
  fun clear()
  fun undo()
  fun redo()
  fun togglePanelType()
}

sealed class UtilityEffect : VMSideEffect()


class UtilityViewModel(
  private val getUIState: GetUIState,
  private val currentVoice: CurrentVoice,
  private val toggleVoiceUC: ToggleVoice,
  private val getPanelLayout: GetPanelLayout,
  private val togglePanelLayout: TogglePanelLayout,
  private val undoUC: Undo,
  private val redoUC: Redo,
  private val clearSelectionUC:ClearSelection,
  private val zoomInUC: ZoomIn,
  private val zoomOutUC: ZoomOut,
  private val handleDeletePress: HandleDeletePress,
  private val handleDeleteLongPress: HandleDeleteLongPress
) : BaseViewModel<UtilityModel, UtilityInterface, UtilityEffect>(),
  UtilityInterface {

  init {
    viewModelScope.launch {
      currentVoice().collect { voice ->
        update { copy(voice = voice) }
      }
    }
    viewModelScope.launch {
      getPanelLayout().collect { layout ->
        update{ copy(panelType = layout)}
      }
    }
    viewModelScope.launch {
      getUIState().collect { state ->
        update { copy(deleteSelected = state == UIState.Delete) }
      }
    }
  }

  override suspend fun initState(): Result<UtilityModel> {
    return UtilityModel(false, 1, true, LayoutID.KEYBOARD).ok()
  }

  override fun getInterface() = this

  override fun delete() {
    handleDeletePress()
  }

  override fun deleteLong() {
    handleDeleteLongPress()
  }

  override fun toggleVoice() {
    toggleVoiceUC()
  }

  override fun zoomIn() {
    zoomInUC()
  }

  override fun zoomOut() {
    zoomOutUC()
  }

  override fun clear() {
    clearSelectionUC()
  }

  override fun undo() {
    undoUC()
  }

  override fun redo() {
    redoUC()
  }

  override fun togglePanelType() {
    togglePanelLayout().ok()
  }
}