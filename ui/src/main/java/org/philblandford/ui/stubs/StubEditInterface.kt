package org.philblandford.ui.stubs

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import kotlinx.coroutines.flow.Flow
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.edit.viewmodel.EditInterface

class StubEditInterface : EditInterface {
  override fun reset() {
    TODO("Not yet implemented")
  }

  override fun getSideEffects(): Flow<VMSideEffect> {
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
}