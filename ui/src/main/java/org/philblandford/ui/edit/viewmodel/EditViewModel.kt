package org.philblandford.ui.edit.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.types.EventParam
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.edit.MoveSelectedArea
import org.philblandford.ascore2.features.input.usecases.DeleteSelectedEvent
import org.philblandford.ascore2.features.insert.UpdateEvent
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
  fun <T> updateParam(eventParam: EventParam, value: T)
  fun move(x: Int, y: Int)
  fun delete()
  fun clear()
}

open class EditViewModel(
  private val getUIState: GetUIState,
  private val updateEvent: UpdateEvent,
  private val deleteSelectedEvent: DeleteSelectedEvent,
  private val moveSelectedArea: MoveSelectedArea
) : BaseViewModel<EditModel, EditInterface, VMSideEffect>(), EditInterface {

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

  override fun <T> setType(type: T) {
    receiveAction { model ->
      updateEvent(model.editItem.event.eventType, EventParam.TYPE, type, model.editItem.address)
      model.ok()
    }
  }

  override fun delete() {
    deleteSelectedEvent()
  }

  override fun move(x: Int, y: Int) {
    moveSelectedArea(x, y)
  }

  override fun clear() {
    receiveAction { model ->
      val newEvent = model.editItem.event.removeParam(EventParam.HARD_START)
      updateEvent(model.editItem.event.eventType, EventParam.HARD_START, null, model.editItem.address)
      model.ok()
    }
  }
}