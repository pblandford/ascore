package org.philblandford.ascore2.features.input.usecases

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CurrentVoice {
  operator fun invoke():StateFlow<Int>
}