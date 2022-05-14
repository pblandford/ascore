package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class CutImpl(private val kScore: KScore, private val clearSelection: ClearSelection) : Cut {
  override operator fun invoke() {
    kScore.cut()
    clearSelection()
  }
}