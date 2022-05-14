package org.philblandford.ascore2.features.ui.usecases

import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.ui.model.UIState

interface GetUIState {

  operator fun invoke():StateFlow<UIState>
}