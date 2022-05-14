package org.philblandford.ascore2.features.scorelayout.usecases

import com.philblandford.kscore.api.KScore

class GetScoreLayoutImpl(private val kScore: KScore) :
  GetScoreLayout {
  override operator fun invoke(): ScoreLayout = ScoreLayout(
    kScore.getPageWidth(),
    kScore.getPageHeight(),
    kScore.getNumPages()
  )
}