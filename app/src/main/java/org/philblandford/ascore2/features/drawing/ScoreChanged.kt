package org.philblandford.ascore2.features.drawing

import kotlinx.coroutines.flow.Flow

interface ScoreChanged {
  operator fun invoke(): Flow<Unit>
}