package org.philblandford.ascore2.features.instruments

import com.philblandford.kscore.api.Instrument

interface GetAvailableInstruments {
  operator fun invoke():List<Instrument>
}