package org.philblandford.ascore2.features.ui.usecases

import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class UpdateInsertParamsImpl(private val uiStateRepository: UiStateRepository) : UpdateInsertParams {
  override operator fun invoke(paramFunc: ParamMap.() -> ParamMap) {
    (uiStateRepository.getUIState().value as? UIState.Insert)?.let { state ->
      uiStateRepository.setInsertItem(state.insertItem.copy(
        params = state.insertItem.params.paramFunc()))
    }
  }
}