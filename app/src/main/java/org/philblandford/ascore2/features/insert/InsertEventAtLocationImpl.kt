package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location
import com.philblandford.kscore.engine.types.Event
import org.philblandford.ascore2.util.failure
import org.philblandford.ascore2.util.ok
import org.philblandford.ascore2.util.tryResult

class InsertEventAtLocationImpl(private val kScore: KScore) : InsertEventAtLocation {
  override fun invoke(location: Location, event:Event):Result<Boolean> {
    return tryResult {
      kScore.getEventAddress(location)?.let { addresss ->
        kScore.addEvent(event.eventType, addresss, event.params).ok()
      } ?: failure("location not found")
    }
  }
}