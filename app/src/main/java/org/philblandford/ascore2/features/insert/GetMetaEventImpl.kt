package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eZero

class GetMetaEventImpl(private val kScore: KScore) : GetMetaEvent {
  override fun invoke(eventType: EventType): Event? {
    return kScore.getEvent(eventType, eZero())
  }
}