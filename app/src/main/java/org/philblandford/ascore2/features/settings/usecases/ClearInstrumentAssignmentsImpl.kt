package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.KScore

class ClearInstrumentAssignmentsImpl(private val instrumentGetter: InstrumentGetter) : ClearInstrumentAssignments {
  override fun invoke() {
    instrumentGetter.clearUser()
  }
}