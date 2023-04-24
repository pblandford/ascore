package org.philblandford.ascore2.features.score

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.NewScoreDescriptor
import org.philblandford.ascore2.features.input.usecases.UpdateInputState

class CreateScoreImpl(private val kScore: KScore) : CreateScore {
  override operator fun invoke(newScoreDescriptor: NewScoreDescriptor):Result<Unit> {
    kScore.createScore(newScoreDescriptor)
    return Result.success(Unit)
  }
}