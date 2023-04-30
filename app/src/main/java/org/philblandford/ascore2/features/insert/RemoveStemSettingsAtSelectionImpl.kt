package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class RemoveStemSettingsAtSelectionImpl(private val kScore: KScore,
private val uiStateRepository: UiStateRepository): RemoveStemSettingsAtSelection {

  override fun invoke() {
    kScore.setStemAtSelected(null, uiStateRepository.getVoice().value)
  }
}