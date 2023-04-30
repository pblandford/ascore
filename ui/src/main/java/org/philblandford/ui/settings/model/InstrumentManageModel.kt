package org.philblandford.ui.settings.model

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import org.philblandford.ui.base.viewmodel.VMModel


data class InstrumentManageModel(
  val groups: List<InstrumentGroup>,
  val selectedInstrument: Instrument? = null
) : VMModel()
