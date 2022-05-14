package org.philblandford.ascore2.features.ui.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ascore2.features.ui.model.UIState

class UiStateRepository {
  private val coroutineScope  = CoroutineScope(Dispatchers.Main)

  private val _voice = MutableStateFlow(1)
  private var _uiState = MutableStateFlow<UIState>(UIState.Input)

    fun setInsertItem(insertItem: InsertItem) {
        coroutineScope.launch {
          (_uiState.value as? UIState.Insert)?.let { insertState ->
            _uiState.emit(insertState.copy(insertItem = insertItem))
          }
        }
    }

  fun getInsertItem():InsertItem? = (_uiState.value as? UIState.Insert)?.insertItem

  fun setVoice(v:Int) {
    coroutineScope.launch {
      _voice.emit(v)
    }
  }

  fun setUiState(state: UIState) {
    coroutineScope.launch {
      _uiState.emit(state)
    }
  }

  fun getUIState():StateFlow<UIState> = _uiState

  fun getVoice():StateFlow<Int> = _voice
}