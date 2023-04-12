package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventType

interface GetMetaEvent {
  operator fun invoke(eventType: EventType): Event?
}