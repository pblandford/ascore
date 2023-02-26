package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.Location
import com.philblandford.kscore.engine.types.Event

interface InsertEventAtLocation {
  operator fun invoke(location: Location, event:Event):Result<Boolean>
}