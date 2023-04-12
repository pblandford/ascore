package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.eZero

class InsertMetaEventImpl(private val kScore: KScore) : InsertMetaEvent {
  override fun invoke(eventType: EventType, params:ParamMap) {
    kScore.addEvent(eventType, eZero(), params)
  }
}