package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

interface UpdateEventParam {
  operator fun <T>invoke(eventType: EventType, eventParam: EventParam, value:T, eventAddress: EventAddress)
}