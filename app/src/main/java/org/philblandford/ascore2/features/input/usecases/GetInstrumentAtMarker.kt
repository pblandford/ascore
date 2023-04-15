package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.Instrument

interface GetInstrumentAtMarker {
  operator fun invoke(): Instrument?

}