package org.philblandford.ui.insert.model

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.base.viewmodel.VMInterface

data class InsertCombinedState<M>(val state: M?, val insertItem: InsertItem?)

interface InsertInterface<M> : VMInterface {
  fun initialise(insertModel: M)
  fun <T>setParam(key:EventParam, value:T)
  fun updateParams(params:ParamMap)
  fun setEventType(eventType: EventType)

  fun getInsertState(): StateFlow<InsertCombinedState<M>>
  fun back()

  fun toggleHelp()
}

interface DefaultInsertInterface : InsertInterface<InsertModel>