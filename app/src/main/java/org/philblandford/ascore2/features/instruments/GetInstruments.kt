package org.philblandford.ascore2.features.instruments

import com.philblandford.kscore.api.Instrument

interface GetInstruments {
  operator fun invoke():List<Instrument>
}