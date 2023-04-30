package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class SetStemsAtSelectionImpl(private val kScore: KScore,
private val uiStateRepository: UiStateRepository) : SetStemsAtSelection {
  override fun invoke() {
    kScore.getStartSelect()?.let { start ->
      kScore.getEndSelect()?.let { end ->
        val events = kScore.getEvents(EventType.DURATION, start, end)
        val grouped = events.toList().groupBy { it.second.isTrue(EventParam.IS_UPSTEM) }
        val isUp = (grouped[true]?.size ?: 0) > (grouped[false]?.size ?: 0)
        kScore.setStemAtSelected(!isUp, uiStateRepository.getVoice().value)
      }
    }
  }
}