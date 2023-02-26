package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class MoveSelectionImpl(private val kScore: KScore) : MoveSelection {
  override operator fun invoke(left:Boolean) {
      kScore.moveSelection(left)
  }
}