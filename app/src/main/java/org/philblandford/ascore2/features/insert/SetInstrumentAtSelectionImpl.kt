package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.KScore

class SetInstrumentAtSelectionImpl(private val kScore: KScore) : SetInstrumentAtSelection {

  override fun invoke(instrument: Instrument) {
    kScore.setInstrumentAtSelection(instrument)
  }
}