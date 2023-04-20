package org.philblandford.ui.stubs

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import kotlinx.coroutines.flow.Flow
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.edit.items.instrumentedit.viewmodel.InstrumentEditInterface

class StubInstrumentEditInterface : InstrumentEditInterface {
  override fun reset() {
    TODO("Not yet implemented")
  }

  override fun getSideEffects(): Flow<VMSideEffect> {
    TODO("Not yet implemented")
  }

  override fun instruments(): List<InstrumentGroup> {
    TODO("Not yet implemented")
  }

  override fun <T> setType(type: T) {
    TODO("Not yet implemented")
  }

  override fun setTypeParam(param: EventParam) {
    TODO("Not yet implemented")
  }

  override fun <T> updateParam(eventParam: EventParam, value: T) {
    TODO("Not yet implemented")
  }

  override fun updateParams(params: ParamMap) {
    TODO("Not yet implemented")
  }

  override fun move(x: Int, y: Int, param: EventParam) {
    TODO("Not yet implemented")
  }

  override fun delete() {
    TODO("Not yet implemented")
  }

  override fun clear() {
    TODO("Not yet implemented")
  }

  override fun setInstrument(instrument: Instrument) {
    TODO("Not yet implemented")
  }

  override fun selectedInstrument(): Instrument? {
    TODO("Not yet implemented")
  }
}