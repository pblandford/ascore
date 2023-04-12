package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

class InsertLyricAtMarkerImpl(
  private val kScore: KScore,
) : InsertLyricAtMarker {

  override fun invoke(text: String, number: Int, moveMarker: Boolean) {
    kScore.getMarker()?.let { marker ->
      val lyricAddress = marker.copy(voice = 1, id = number)
      val existing = kScore.getEvent(EventType.LYRIC, lyricAddress) ?: Event(
        EventType.LYRIC
      )
      if (text.isNotEmpty()) {
        kScore.batch(
          {
            if (moveMarker) {
              kScore.moveMarker(false)
            }
          },
          {
            kScore.addEvent(
              EventType.LYRIC,
              lyricAddress,
              existing.params + (EventParam.TEXT to text) + (EventParam.NUMBER to number)
            )
          }

        )
      } else {
        kScore.deleteEvent(EventType.LYRIC, lyricAddress)
      }
    }
  }
}