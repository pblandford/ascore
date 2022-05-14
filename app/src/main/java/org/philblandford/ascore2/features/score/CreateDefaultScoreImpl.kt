package org.philblandford.ascore2.features.score

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.util.ok

class CreateDefaultScoreImpl(private val kScore: KScore) : CreateDefaultScore {
  override operator fun invoke():Result<Unit> {
    kScore.createDefaultScore()
    return Unit.ok()
  }
}