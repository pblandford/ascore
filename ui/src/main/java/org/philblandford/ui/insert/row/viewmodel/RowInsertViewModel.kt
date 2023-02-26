package org.philblandford.ui.insert.row.viewmodel

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.model.stubItem
import org.philblandford.ascore2.features.ui.usecases.GetInsertItem
import org.philblandford.ascore2.features.ui.usecases.InsertItemMenu
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertParams
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel

data class RowInsertModel<T>(
  val ids: List<Pair<Int, T>>,
  val selected: Int,
) : InsertModel()

interface RowInsertInterface<T> : InsertInterface<RowInsertModel<T>> {
  fun selectItem(idx: Int)

  fun setParamType(eventParam: EventParam)
}


open class RowInsertViewModel<T>(ids:List<Pair<Int, T>>, selected: Int = 0
) : InsertViewModel<RowInsertModel<T>, RowInsertInterface<T>>(),
  RowInsertInterface<T> {

  private var param:EventParam = EventParam.TYPE

  private val initState = RowInsertModel(ids, selected)

  override suspend fun initState(): Result<RowInsertModel<T>> {
    return initState.ok()
  }

  override fun getInterface() = this

  override fun setParamType(eventParam: EventParam) {
    param = eventParam
  //  update { copy(selected = ids.indexOfFirst { it.second == params[param] }) }
  }

  override fun selectItem(idx: Int) {
    getState().value?.let { state ->
      setParam(param, state.ids[idx].second)
    }
  }
}