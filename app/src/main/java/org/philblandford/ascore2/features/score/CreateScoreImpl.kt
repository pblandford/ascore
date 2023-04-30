package org.philblandford.ascore2.features.score

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.NewScoreDescriptor
import org.philblandford.ascore2.features.input.usecases.UpdateInputState
import org.philblandford.ascore2.features.settings.usecases.UpdateFontOptions

class CreateScoreImpl(private val kScore: KScore,
                      private val updateFontOptions: UpdateFontOptions) : CreateScore {
  override operator fun invoke(newScoreDescriptor: NewScoreDescriptor):Result<Unit> {
    kScore.createScore(newScoreDescriptor)
    updateFontOptions()
    return Result.success(Unit)
  }
}