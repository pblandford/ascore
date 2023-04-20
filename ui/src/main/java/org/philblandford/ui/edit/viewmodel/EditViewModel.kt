package org.philblandford.ui.edit.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.paramMapOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.edit.MoveSelectedArea
import org.philblandford.ascore2.features.input.usecases.DeleteSelectedEvent
import org.philblandford.ascore2.features.insert.InsertEvent
import org.philblandford.ascore2.features.insert.UpdateEventParam
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.usecases.GetUIState
import org.philblandford.ascore2.util.failure
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.edit.model.EditModel

interface EditInterface : VMInterface {
  fun <T> setType(type: T)
  fun setTypeParam(param:EventParam)
  fun <T> updateParam(eventParam: EventParam, value: T)
  fun updateParams(params:ParamMap)
  fun move(x: Int, y: Int, param: EventParam = EventParam.HARD_START)
  fun delete()
  fun clear()
}

open class EditViewModel(
  private val getUIState: GetUIState,
  private val updateEvent: UpdateEventParam,
  private val insertEvent: InsertEvent,
  private val deleteSelectedEvent: DeleteSelectedEvent,
  private val moveSelectedArea: MoveSelectedArea
) : BaseViewModel<EditModel, EditInterface, VMSideEffect>(), EditInterface {

  private var typeParam = EventParam.TYPE

  init {
    viewModelScope.launch {
      getUIState().collectLatest { state ->
        (state as? UIState.Edit)?.let { edit ->
          update { copy(editItem = edit.editItem) }
        }
      }
    }
  }

  override suspend fun initState(): Result<EditModel> {
    return (getUIState().value as? UIState.Edit)?.let { state ->
      EditModel(state.editItem)
    }?.ok() ?: failure("Could not get edit state")
  }

  override fun getInterface(): EditInterface = this

  override fun <T> updateParam(eventParam: EventParam, value: T) {
    receiveAction {
      updateEvent(it.editItem.event.eventType, eventParam, value, it.editItem.address)
      it.ok()
    }
  }

  override fun updateParams(params: ParamMap) {
    receiveAction {
      (params.toList() - it.editItem.event.params.toList().toSet()).firstOrNull()?.let { param ->
        updateEvent(it.editItem.event.eventType, param.first, param.second, it.editItem.address)
        it.ok()
      } ?: it.ok()
    }
  }

  override fun <T> setType(type: T) {
    receiveAction { model ->
      updateEvent(model.editItem.event.eventType, typeParam, type, model.editItem.address)
      model.ok()
    }
  }

  override fun setTypeParam(param: EventParam) {
    typeParam = param
  }

  override fun delete() {
    deleteSelectedEvent()
  }

  override fun move(x: Int, y: Int, param: EventParam) {
    moveSelectedArea(x, y, param)
  }

  override fun clear() {
    getState().value?.editItem?.let { editItem ->
      insertEvent(editItem.event.eventType, editItem.address, editItem.event.params  +
        paramMapOf(
          EventParam.HARD_START to null,
          EventParam.HARD_MID to null,
          EventParam.HARD_END to null,
      ))
    }
  }
}