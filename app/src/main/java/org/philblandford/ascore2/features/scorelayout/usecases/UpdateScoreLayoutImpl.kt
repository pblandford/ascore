package org.philblandford.ascore2.features.scorelayout.usecases

import org.philblandford.ascore2.features.scorelayout.repository.ScoreLayoutRepository

class UpdateScoreLayoutImpl(private val scoreLayoutRepository: ScoreLayoutRepository) :
  UpdateScoreLayout {
  override operator fun invoke() {
    scoreLayoutRepository.updateScoreLayout()
  }
}