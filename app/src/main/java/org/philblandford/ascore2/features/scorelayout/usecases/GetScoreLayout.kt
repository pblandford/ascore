package org.philblandford.ascore2.features.scorelayout.usecases

import kotlinx.coroutines.flow.StateFlow

data class ScoreLayout(
  val width:Int,
  val height:Int,
  val numPages:Int
)

interface GetScoreLayout {
  operator fun invoke(): ScoreLayout
}