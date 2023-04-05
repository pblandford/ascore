package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventType

class GetMetaEventImpl(private val kScore: KScore) : GetMetaEvent {
  override fun invoke(eventType: EventType): String {
    return kScore.getMeta(eventType) ?: ""
  }
}