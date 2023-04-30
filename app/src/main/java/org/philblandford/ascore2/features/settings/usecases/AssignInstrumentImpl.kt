package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.api.InstrumentGetter

class AssignInstrumentImpl(private val instrumentGetter: InstrumentGetter) : AssignInstrument {

  override fun invoke(instrument: String, group: String) {
    instrumentGetter.assignInstrument(instrument, group)
  }
}