package org.philblandford.ui.insert.items.barnumbering.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.types.BarNumbering
import com.philblandford.kscore.engine.types.EventParam
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.settings.usecases.GetOption
import org.philblandford.ascore2.features.settings.usecases.SetOption
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.items.barnumbering.model.BarNumberingModel
import org.philblandford.ui.insert.model.InsertInterface

interface BarNumberingInterface : InsertInterface<BarNumberingModel> {
  fun setOption(option: Any)
}

class BarNumberingViewModel(
  private val getOption: GetOption,
  private val setOption: SetOption) : InsertViewModel<BarNumberingModel, BarNumberingInterface>(), BarNumberingInterface {

  init {
    viewModelScope.launch {
      scoreUpdate().collectLatest {
        update {
          BarNumberingModel(getOption(EventParam.OPTION_BAR_NUMBERING) ?: 4)
        }
      }
    }
  }

  override suspend fun initState(): Result<BarNumberingModel> {
    return BarNumberingModel(getOption(EventParam.OPTION_BAR_NUMBERING) ?: 4).ok()
  }

  override fun getInterface(): BarNumberingInterface = this

  override fun setOption(option:Any) {
    setOption(EventParam.OPTION_BAR_NUMBERING, option)
  }
}