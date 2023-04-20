package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

class UpdateEventParamImpl(private val kScore: KScore) : UpdateEventParam {
  override fun <T>invoke(eventType: EventType, eventParam:EventParam, value:T, eventAddress: EventAddress) {
    kScore.setParam(eventType, eventParam, value, eventAddress)
  }
}