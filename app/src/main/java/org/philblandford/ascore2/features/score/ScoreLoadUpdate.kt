package org.philblandford.ascore2.features.score

import kotlinx.coroutines.flow.Flow

interface ScoreLoadUpdate {
  operator fun invoke(): Flow<Unit>
}