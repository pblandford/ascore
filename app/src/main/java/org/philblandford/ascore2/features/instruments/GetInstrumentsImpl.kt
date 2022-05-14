package org.philblandford.ascore2.features.instruments

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.KScore

class GetInstrumentsImpl(private val kScore: KScore) : GetInstruments {
  override operator fun invoke():List<Instrument> {
    return kScore.getInstrumentsInScore()
  }
}