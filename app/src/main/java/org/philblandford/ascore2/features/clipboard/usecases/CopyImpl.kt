package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class CopyImpl(private val kScore: KScore, private val clearSelection: ClearSelection) : Copy {
  override operator fun invoke() {
    kScore.copy()
    clearSelection()
  }
}