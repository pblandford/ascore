package org.philblandford.ascore2.features.drawing

import com.philblandford.kscore.api.KScore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ScoreChangedImpl(private val kScore: KScore) : ScoreChanged {
  override operator fun invoke(): Flow<Unit> {
    return kScore.representationUpdate().combine(kScore.selectionUpdate()) { _, _ ->
    }
  }
}