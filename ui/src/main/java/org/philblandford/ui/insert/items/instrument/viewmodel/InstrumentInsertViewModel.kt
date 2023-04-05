package org.philblandford.ui.insert.items.instrument.viewmodel

import com.philblandford.kscore.api.Instrument
import org.philblandford.ascore2.features.instruments.GetAvailableInstruments
import org.philblandford.ascore2.features.instruments.GetInstruments
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.items.instrument.model.InstrumentInsertModel
import org.philblandford.ui.insert.model.InsertInterface

interface InstrumentInsertInterface : InsertInterface<InstrumentInsertModel> {
  fun setInstrument(instrument:Instrument)
}

class InstrumentInsertViewModel(private val getAvailableInstruments: GetAvailableInstruments) :
  InsertViewModel<InstrumentInsertModel, InstrumentInsertInterface>(),
  InstrumentInsertInterface {

  override suspend fun initState(): Result<InstrumentInsertModel> {
    val instruments = getAvailableInstruments()
    return InstrumentInsertModel(instruments).ok()
  }

  override fun getInterface(): InstrumentInsertInterface = this

  override fun setInstrument(instrument: Instrument) {
    updateParams(instrument.toEvent().params)
  }
}