package org.philblandford.ascore2.features.score

import com.philblandford.kscore.api.KScore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ScoreUpdateImpl(private val kScore: KScore) : ScoreUpdate {
  override fun invoke(): Flow<Unit> {
    return kScore.scoreUpdate()
  }
}