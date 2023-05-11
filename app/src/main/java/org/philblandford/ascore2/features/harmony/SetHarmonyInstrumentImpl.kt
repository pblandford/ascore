package org.philblandford.ascore2.features.harmony

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.KScore

class SetHarmonyInstrumentImpl(private val kScore: KScore) : SetHarmonyInstrument {
  override fun invoke(instrument: Instrument) {
    kScore.setHarmonyPlaybackInstrument(instrument)
  }
}