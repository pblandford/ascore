package org.philblandford.ascore2.features.error

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.core.score.ScoreError
import kotlinx.coroutines.flow.Flow

class GetErrorFlowImpl(private val kScore: KScore) : GetErrorFlow {

  override fun invoke(): Flow<ScoreError> {
    return kScore.getErrorFlow()
  }
}