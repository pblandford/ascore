package org.philblandford.ascore2.features.ui.usecases

import android.text.Layout
import com.philblandford.kscore.api.KScore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class GetPanelLayoutImpl(
  private val kScore: KScore,
  private val uiStateRepository: UiStateRepository
) : GetPanelLayout {
  private val layoutFlow = MutableStateFlow(LayoutID.KEYBOARD)
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  init {
    coroutineScope.launch {

      uiStateRepository.getUIState().collectLatest {
        val layout = when (it) {
          UIState.Input -> {
            if (kScore.getInstrumentAtMarker()?.percussion == true) {
              LayoutID.PERCUSSION
            } else {
              LayoutID.KEYBOARD
            }
          }
          is UIState.Insert -> {
            it.insertItem.layoutID
          }
          UIState.InsertChoose -> LayoutID.INSERT_CHOOSE
          else -> LayoutID.EMPTY
        }
        layoutFlow.emit(layout)
      }
    }
  }

  override operator fun invoke(): StateFlow<LayoutID> {
    return layoutFlow

  }
}