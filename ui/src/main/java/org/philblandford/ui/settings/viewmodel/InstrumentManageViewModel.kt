package org.philblandford.ui.settings.viewmodel

import com.philblandford.kscore.api.Instrument
import org.philblandford.ascore2.features.instruments.GetAvailableInstruments
import org.philblandford.ascore2.features.settings.usecases.AssignInstrument
import org.philblandford.ascore2.features.settings.usecases.ClearInstrumentAssignments
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.settings.model.InstrumentManageModel

interface InstrumentManageInterface : VMInterface {
  fun clear()
  fun setSelectedInstrument(instrument: Instrument?)
  fun assignInstrument(name: String, group: String)
}

class InstrumentManageViewModel(private val getAvailableInstruments: GetAvailableInstruments,
private val assignInstrumentUC: AssignInstrument,
private val clearInstrumentAssignments: ClearInstrumentAssignments) :
  BaseViewModel<InstrumentManageModel, InstrumentManageInterface, VMSideEffect>(),
  InstrumentManageInterface {

  override suspend fun initState(): Result<InstrumentManageModel> {
    return InstrumentManageModel(getAvailableInstruments()).ok()
  }

  override fun getInterface(): InstrumentManageInterface = this

  override fun clear() {
    clearInstrumentAssignments()
    update { InstrumentManageModel(getAvailableInstruments(), null) }
  }

  override fun setSelectedInstrument(instrument: Instrument?) {
    update { copy(selectedInstrument = instrument) }
  }

  override fun assignInstrument(name: String, group: String) {
    assignInstrumentUC(name, group)
    update { InstrumentManageModel(getAvailableInstruments(), null) }
  }
}