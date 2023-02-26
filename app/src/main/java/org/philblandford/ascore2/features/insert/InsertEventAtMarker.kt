package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.engine.types.Event

interface InsertEventAtMarker {
  operator fun invoke(event:Event):Result<Unit>
}