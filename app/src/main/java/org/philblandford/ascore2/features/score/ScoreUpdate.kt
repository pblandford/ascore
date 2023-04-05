package org.philblandford.ascore2.features.score

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ScoreUpdate {
  operator fun invoke():Flow<Unit>
}