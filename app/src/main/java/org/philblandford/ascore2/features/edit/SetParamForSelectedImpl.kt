package org.philblandford.ascore2.features.edit

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

class SetParamForSelectedImpl(private val kScore: KScore) : SetParamForSelected {

  override fun <T> invoke(eventType: EventType, eventParam: EventParam, value: T) {
    kScore.setParamAtSelection(eventType, eventParam, value)
  }
}