package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap

interface InsertMetaEvent {

  operator fun invoke(eventType: EventType, params:ParamMap)
}