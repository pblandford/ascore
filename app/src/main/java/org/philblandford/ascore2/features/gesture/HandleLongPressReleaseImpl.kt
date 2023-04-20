package org.philblandford.ascore2.features.gesture

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventType
import org.philblandford.ascore2.features.ui.model.EditItem
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository
import timber.log.Timber

class HandleLongPressReleaseImpl(private val uiStateRepository: UiStateRepository,
                                 private val kScore: KScore
) : HandleLongPressRelease {

  override fun invoke() {
    when (uiStateRepository.getUIState().value) {
      is UIState.MoveNote -> {
        uiStateRepository.setUiState(UIState.Clipboard)
      }
      else -> {}
    }
  }

}