package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

interface UpdateEvent {
  operator fun <T>invoke(eventType: EventType, eventParam: EventParam, value:T, eventAddress: EventAddress)
}