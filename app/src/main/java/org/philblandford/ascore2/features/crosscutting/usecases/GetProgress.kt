package org.philblandford.ascore2.features.crosscutting.usecases

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface GetProgress {
  operator fun invoke():StateFlow<Boolean>
}