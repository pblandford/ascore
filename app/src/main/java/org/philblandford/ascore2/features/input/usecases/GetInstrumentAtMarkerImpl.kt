package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.KScore

class GetInstrumentAtMarkerImpl(private val kScore: KScore) : GetInstrumentAtMarker {
  override fun invoke(): Instrument? {
    return kScore.getInstrumentAtMarker()
  }
}