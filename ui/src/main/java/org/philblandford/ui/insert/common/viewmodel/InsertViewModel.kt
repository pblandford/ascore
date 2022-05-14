package org.philblandford.ui.insert.common.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.ui.usecases.GetInsertItem
import org.philblandford.ascore2.features.ui.usecases.InsertItemMenu
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertItem
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel


abstract class InsertViewModel<M : InsertModel, I : InsertInterface<M>>(
  private val updateInsertItem: UpdateInsertItem,
  private val getInsertItem: GetInsertItem,
  private val insertItemMenu: InsertItemMenu
) : BaseViewModel<M, I, VMSideEffect>(), InsertInterface<M> {

  init {
    viewModelScope.launch {
      getInsertItem().collectLatest { item ->
        item?.let {
          update { updateParams(item.params) }
        }
      }
    }
  }

  override fun back() {
    insertItemMenu()
  }

  abstract fun M.updateParams(params: ParamMap): M

  override fun initialise(insertModel: M) {
    update { insertModel }
    updateInsertItem { insertModel.params }
  }

  override fun <T> setParam(key: EventParam, value: T) {
    updateInsertItem { this + (key to value) }
  }

}