package org.philblandford.ascore2.features.error

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreError
import kotlinx.coroutines.flow.Flow

interface GetErrorFlow {
  operator fun invoke(): Flow<ScoreError>
}