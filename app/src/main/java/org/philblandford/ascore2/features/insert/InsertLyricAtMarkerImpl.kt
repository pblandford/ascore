package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

class InsertLyricAtMarkerImpl(
  private val kScore: KScore,
) : InsertLyricAtMarker {

  override fun invoke(text: String, number: Int) {
    kScore.getMarker()?.let { marker ->
      val existing = kScore.getEvent(EventType.LYRIC, marker.copy(id = number)) ?: Event(
        EventType.LYRIC
      )
      kScore.addEvent(
        EventType.LYRIC,
        marker,
        existing.params + (EventParam.TEXT to text) + (EventParam.NUMBER to number)
      )
    }
  }
}