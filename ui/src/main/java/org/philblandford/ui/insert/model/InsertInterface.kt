package org.philblandford.ui.insert.model

import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.base.viewmodel.VMInterface

interface InsertInterface<M : InsertModel> : VMInterface {
  fun initialise(insertModel: M)
  fun <T>setParam(key:EventParam, value:T)
  fun back()
}