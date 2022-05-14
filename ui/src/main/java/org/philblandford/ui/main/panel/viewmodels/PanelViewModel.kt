package org.philblandford.ui.main.panel.viewmodels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ascore2.features.ui.usecases.GetPanelLayout
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import timber.log.Timber

data class PanelModel(
  val layoutID: LayoutID
) : VMModel()

class PanelViewModel(private val getPanelLayout: GetPanelLayout)
  : BaseViewModel<PanelModel, VMInterface, VMSideEffect>(), VMInterface {

  init {
    viewModelScope.launch {
      getPanelLayout().collectLatest { layoutID ->
        Timber.e("Panel $layoutID")
        update { copy(layoutID = layoutID) }
      }
    }
  }

  override suspend fun initState(): Result<PanelModel> {
    return PanelModel(getPanelLayout().value).ok()
  }

  override fun getInterface() = this
}