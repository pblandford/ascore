package org.philblandford.ascore2.features.ui.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ascore2.features.ui.model.UIState
import kotlin.math.sign

class UiStateRepository {
  private val coroutineScope = CoroutineScope(Dispatchers.Main)

  private val _voice = MutableStateFlow(1)
  private var _uiState = MutableStateFlow<UIState>(UIState.Input)
  private var _drag = MutableStateFlow(0f to 0f)

  fun setInsertItem(insertItem: InsertItem) {
    coroutineScope.launch {
      (_uiState.value as? UIState.Insert)?.let { insertState ->
        _uiState.emit(insertState.copy(insertItem = insertItem))
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
}