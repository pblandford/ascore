package org.philblandford.ui.insert.common.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.inject
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.usecases.GetHelpKey
import org.philblandford.ascore2.features.ui.usecases.GetInsertItem
import org.philblandford.ascore2.features.ui.usecases.InsertItemMenu
import org.philblandford.ascore2.features.ui.usecases.SetHelpKey
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertEvent
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertParams
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.insert.model.InsertCombinedState
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import timber.log.Timber


abstract class InsertViewModel<M : InsertModel, I : InsertInterface<M>> :
  BaseViewModel<M, I, VMSideEffect>(), InsertInterface<M> {
  protected val updateInsertParams: UpdateInsertParams by inject()
  protected val updateEventType: UpdateInsertEvent by inject()
  protected val getInsertItemUC: GetInsertItem by inject()
  protected val insertItemMenu: InsertItemMenu by inject()
  protected val setHelpKey:SetHelpKey by inject()
  protected val getHelpKey:GetHelpKey by inject()

  private val combinedState =
    getInsertItemUC().combine(getState()) { insertItem, state ->
      Timber.e("Combined $insertItem $state")
      InsertCombinedState(state, insertItem)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, InsertCombinedState(null as M?, null))

  override fun getInsertState(): StateFlow<InsertCombinedState<M>> {
    return combinedState
  }

  override fun back() {
    insertItemMenu()
  }

  override fun updateParams(params: ParamMap) {
    updateInsertParams { params }
  }

  override fun initialise(insertModel: M) {
    update { insertModel }
  }

  override fun <T> setParam(key: EventParam, value: T) {
    updateInsertParams { this + (key to value) }
  }

  override fun setEventType(eventType: EventType) {
    updateEventType(eventType)
  }

  override fun toggleHelp() {
    getInsertItem()?.let { item ->
      getHelpKey().value?.let {
        setHelpKey(null)
      } ?: run {
        setHelpKey(item.helpTag)
      }
    }
  }

  protected fun getInsertItem():InsertItem? = getInsertState().value.insertItem
}