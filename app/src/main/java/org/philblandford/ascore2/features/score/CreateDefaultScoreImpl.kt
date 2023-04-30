package org.philblandford.ascore2.features.score

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.settings.usecases.UpdateFontOptions
import org.philblandford.ascore2.util.ok

class CreateDefaultScoreImpl(
  private val kScore: KScore,
  private val updateFontOptions: UpdateFontOptions
) : CreateDefaultScore {
  override operator fun invoke(): Result<Unit> {
    kScore.createDefaultScore()
    updateFontOptions()
    return Unit.ok()
  }
}