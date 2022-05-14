package org.philblandford.ascore2.features.instruments

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGetter

class GetAvailableInstrumentsImpl(private val instrumentGetter: InstrumentGetter) : GetAvailableInstruments {
  override operator fun invoke():List<Instrument> {
    return instrumentGetter.getInstrumentGroups().flatMap { group ->
      group.instruments
    }
  }
}