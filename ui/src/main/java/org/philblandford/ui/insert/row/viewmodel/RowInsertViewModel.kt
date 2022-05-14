package org.philblandford.ui.insert.row.viewmodel

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ascore2.features.ui.usecases.GetInsertItem
import org.philblandford.ascore2.features.ui.usecases.InsertItemMenu
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertItem
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel

data class RowInsertModel<T>(
  override val title:Int,
  override val helpTag:String,
  val ids: List<Pair<Int, T>>,
  val selected: Int,
  override val params: ParamMap = mapOf(
    EventParam.TYPE to ids[selected].second
  )
) : InsertModel(title, helpTag, params)

interface RowInsertInterface<T> : InsertInterface<RowInsertModel<T>> {
  fun selectItem(idx: Int)
}


class RowInsertViewModel<T>(
  updateInsertItem: UpdateInsertItem,
  getInsertItem: GetInsertItem,
  insertItemMenu: InsertItemMenu
) : InsertViewModel<RowInsertModel<T>, RowInsertInterface<T>>(updateInsertItem, getInsertItem,
insertItemMenu),
  RowInsertInterface<T> {

  override suspend fun initState(): Result<RowInsertModel<T>> {
    return RowInsertModel<T>(-1, "", listOf(), 0, mapOf()).ok()
  }

  override fun getInterface() = this

  override fun RowInsertModel<T>.updateParams(params: ParamMap): RowInsertModel<T> {
    return copy(
      params = params,
      selected = ids.indexOfFirst { it.second == params[EventParam.TYPE] })
  }


  override fun selectItem(idx: Int) {
    getState().value?.let { state ->
      setParam(EventParam.TYPE, state.ids[idx].second)
    }
  }
}