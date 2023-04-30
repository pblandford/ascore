package org.philblandford.ascore2.features.edit

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

interface SetParamForSelected {
  operator fun <T>invoke(eventType: EventType, eventParam: EventParam, value:T)
}