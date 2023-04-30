package org.philblandford.ascore2.features.score

import com.philblandford.kscore.api.KScore
import kotlinx.coroutines.flow.Flow

class ScoreLoadUpdateImpl(private val kScore: KScore) : ScoreLoadUpdate {
  override fun invoke(): Flow<Unit> {
    return kScore.scoreLoadUpdate()
  }
}