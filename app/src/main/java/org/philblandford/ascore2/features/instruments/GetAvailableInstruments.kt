package org.philblandford.ascore2.features.instruments

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup

interface GetAvailableInstruments {
  operator fun invoke():List<InstrumentGroup>
}