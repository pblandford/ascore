package org.philblandford.ascore2.features.input.usecases

import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class CurrentVoiceImpl(private val uiStateRepository: UiStateRepository) : CurrentVoice {
  override operator fun invoke():StateFlow<Int> {
    return uiStateRepository.getVoice()
  }
}