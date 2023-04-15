package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.engine.types.EventType

interface GetDefaultTextSize {
  operator fun invoke(eventType: EventType):Int
}