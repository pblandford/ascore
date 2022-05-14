package org.philblandford.ascore2.features.input.usecases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class ToggleVoiceImpl(private val uiStateRepository: UiStateRepository) : ToggleVoice {
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  override operator fun invoke() {
    coroutineScope.launch {
      val newValue = if (uiStateRepository.getVoice().value == 1) 2 else 1
      uiStateRepository.setVoice(newValue)
    }
  }
}