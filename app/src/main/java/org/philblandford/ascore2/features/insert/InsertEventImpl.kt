package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap

class InsertEventImpl(private val kScore: KScore) : InsertEvent{
  override operator fun invoke(type:EventType, eventAddress: EventAddress, params:ParamMap) {
    kScore.addEvent(type, eventAddress, params)
  }
}