package org.philblandford.ui.insert.row.viewmodel

import com.philblandford.kscore.engine.types.EventParam
import org.koin.core.component.inject
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertItem
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

  private val updateInsertItem:UpdateInsertItem by inject()
  private var param:EventParam = EventParam.TYPE

  private val initState = RowInsertModel(ids, selected)

  override suspend fun initState(): Result<RowInsertModel<T>> {
    getInsertState().value.insertItem?.typeParam?.let { type ->
      setParamType(type)
    }
    setParam(param, initState.ids[initState.selected].second)
    return initState.ok().onSuccess { selectItem(it.selected) }
  }

  override fun getInterface() = this

  override fun setParamType(eventParam: EventParam) {
    param = eventParam
  }

  override fun selectItem(idx: Int) {
    getState().value?.let { state ->
      getInsertItem()?.let { insertItem ->
        val type = state.ids[idx].second
        updateInsertItem {
          copy(params = params + (param to type), eventType = insertItem.getEventType(type as Any))
        }
      }

      update { copy(selected = idx) }
    }
  }
}