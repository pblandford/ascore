package org.philblandford.ascore2.features.save

import com.philblandford.kscore.api.KScore

class GetTitleImpl(private val kScore: KScore) : GetTitle {
  override operator fun invoke():String {
    return kScore.getScore()?.getTitle() ?: ""
  }
}