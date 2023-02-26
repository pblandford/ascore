package org.philblandford.ascore2.features.instruments

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.InstrumentGroup

class GetAvailableInstrumentsImpl(private val instrumentGetter: InstrumentGetter) : GetAvailableInstruments {
  override operator fun invoke():List<InstrumentGroup> {
    return instrumentGetter.getInstrumentGroups()
    }
}