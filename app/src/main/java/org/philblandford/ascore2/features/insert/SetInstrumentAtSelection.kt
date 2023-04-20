package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.Instrument

interface SetInstrumentAtSelection {
  operator fun invoke(instrument: Instrument)
}