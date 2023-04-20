package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.KScore

class GetInstrumentAtSelectionImpl(private val kScore: KScore) : GetInstrumentAtSelection {

  override fun invoke(): Instrument? {
    return kScore.getInstrumentAtSelection()
  }
}