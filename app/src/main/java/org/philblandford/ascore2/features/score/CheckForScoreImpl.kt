package org.philblandford.ascore2.features.score

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.startup.StartupManager

class CheckForScoreImpl(private val kScore: KScore) : CheckForScore {
  override fun invoke():Boolean {
    return kScore.getScore() != null
  }
}