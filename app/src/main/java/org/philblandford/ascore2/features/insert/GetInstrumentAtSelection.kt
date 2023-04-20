package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.Instrument

interface GetInstrumentAtSelection {
  operator fun invoke():Instrument?
}