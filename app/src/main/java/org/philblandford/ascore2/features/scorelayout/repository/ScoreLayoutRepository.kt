package org.philblandford.ascore2.features.scorelayout.repository

import com.philblandford.kscore.api.KScore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.scorelayout.usecases.ScoreLayout

class ScoreLayoutRepository(private val kScore: KScore) {
  val scoreLayout = MutableStateFlow(getScoreLayout())
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  fun updateScoreLayout() {
    coroutineScope.launch {
      scoreLayout.emit(getScoreLayout())
    }
  }

  private fun getScoreLayout():ScoreLayout = ScoreLayout(
    kScore.getPageWidth(),
    kScore.getPageHeight(),
    kScore.getNumPages()
  )
}