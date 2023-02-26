package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.Event
import org.philblandford.ascore2.util.ok
import org.philblandford.ascore2.util.tryResult

class InsertEventAtMarkerImpl(private val kScore: KScore):InsertEventAtMarker {
  override operator fun invoke(event:Event):Result<Unit> {
    return tryResult {
      kScore.addEventAtMarker(event.eventType, event.params).ok()
    }
  }
}