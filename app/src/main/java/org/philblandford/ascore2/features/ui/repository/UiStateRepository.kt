package org.philblandford.ascore2.features.ui.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.clipboard.usecases.SelectionUpdate
import org.philblandford.ascore2.features.input.usecases.GetSelectedArea
import org.philblandford.ascore2.features.score.ScoreUpdate
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.model.UIState
import timber.log.Timber
import kotlin.math.sign

class UiStateRepository(
  private val selectionUpdate: SelectionUpdate,
  private val getSelectedArea: GetSelectedArea
) {

  private val coroutineScope = CoroutineScope(Dispatchers.Main)
  private val _voice = MutableStateFlow(1)
  private var _uiState = MutableStateFlow<UIState>(UIState.Input)
  private var _drag = MutableStateFlow(0f to 0f)
  private var _helpKey = MutableStateFlow<String?>(null)

  init {
    coroutineScope.launch {
      selectionUpdate().collectLatest {
        Timber.e("SelectionUpdate")
        when (val state = _uiState.value) {
          is UIState.Edit -> {
            getSelectedArea()?.let { area ->
              Timber.e("MOVEITEM SelectionUpdate area ${area.scoreArea.rectangle}")

              val item = state.editItem.copy(
                event = area.event,
                address = area.eventAddress,
                rectangle = area.scoreArea.rectangle
              )
              setUiState(state.copy(editItem = item))
            }
          }

          else -> {}
        }
      }
    }
  }

  fun setInsertItem(insertItem: InsertItem) {
    (_uiState.value as? UIState.Insert)?.let { insertState ->

      val newState = insertState.copy(insertItem = insertItem)
      _uiState.value = newState

      coroutineScope.launch {
        _uiState.emit(newState)
      }
    }
  }

  fun getInsertItem(): InsertItem? = (_uiState.value as? UIState.Insert)?.insertItem

  fun setVoice(v: Int) {
    coroutineScope.launch {
      _voice.emit(v)
    }
  }

  fun setUiState(state: UIState) {
    coroutineScope.launch {
      _uiState.emit(state)
    }
  }

  fun getUIState(): StateFlow<UIState> = _uiState

  fun getVoice(): StateFlow<Int> = _voice

  fun dragStart() {
    coroutineScope.launch {
      _drag.emit(0f to 0f)
    }
  }

  fun updateDrag(deltaX: Float, deltaY: Float) {
    coroutineScope.launch {
      val x = if (deltaX.sign != _drag.value.first.sign) 0f else _drag.value.first
      val y = if (deltaY.sign != _drag.value.second.sign) 0f else _drag.value.second
      _drag.emit((x + deltaX) to (y + deltaY))
    }
  }

  fun getDrag(): StateFlow<Pair<Float, Float>> = _drag

  fun getHelpKey(): StateFlow<String?> = _helpKey

  fun setHelpKey(key: String?) {
    coroutineScope.launch {
      _helpKey.value = key
    }
  }
}