package org.philblandford.ascore2.features.harmony

import com.philblandford.kscore.api.Instrument

interface SetHarmonyInstrument {
  operator fun invoke(instrument: Instrument)
}