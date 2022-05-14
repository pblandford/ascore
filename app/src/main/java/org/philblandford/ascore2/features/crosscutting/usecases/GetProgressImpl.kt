package org.philblandford.ascore2.features.crosscutting.usecases

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GetProgressImpl : GetProgress, SetProgress {
  private val progressFlow = MutableStateFlow(false)

  override fun invoke(): StateFlow<Boolean> {
    return progressFlow
  }

  override suspend fun invoke(yes: Boolean) {
    progressFlow.emit(yes)
  }
}