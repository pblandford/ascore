package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventType

class GetDefaultTextSizeImpl(private val kScore: KScore) : GetDefaultTextSize {
  override fun invoke(eventType: EventType): Int {
    return kScore.getDefaultTextSize(eventType)
  }
}