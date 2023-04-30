package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class InsertTiesAtSelectionImpl(
  private val kScore: KScore,
  private val uiStateRepository: UiStateRepository
) : InsertTiesAtSelection {
  override fun invoke() {
    kScore.addTieAtSelected(uiStateRepository.getVoice().value)
  }
}