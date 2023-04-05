package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap

interface InsertEvent {
  operator fun invoke(type:EventType, eventAddress: EventAddress, params:ParamMap)
}