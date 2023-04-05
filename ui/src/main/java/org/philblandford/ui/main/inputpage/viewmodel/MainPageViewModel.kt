package org.philblandford.ui.main.inputpage.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.api.Location
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.clipboard.usecases.GetSelection
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr
import org.philblandford.ascore2.features.crosscutting.usecases.GetError
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.usecases.GetUIState
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import timber.log.Timber

data class MainPageModel(
  val showClipboard: Boolean = false,
  val selectedArea: Location? = null
) : VMModel()

sealed class MainPageSideEffect : VMSideEffect() {
  data class Error(val errorDescr: ErrorDescr) : MainPageSideEffect()
}

class MainPageViewModel(private val getUIState: GetUIState,
private val getError: GetError,
private val getSelection: GetSelection) :
  BaseViewModel<MainPageModel, VMInterface, MainPageSideEffect>(), VMInterface {

  init {

    getSelection().combine(getUIState()) { selection, uiState ->
      update {
        copy(showClipboard = uiState == UIState.Clipboard, selectedArea = selection?.startLocation)
      }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Unit)

    viewModelScope.launch {
      getError().collectLatest { error ->
        Timber.e("Error $error")
        launchEffect(MainPageSideEffect.Error(error))
      }
    }
  }

  override suspend fun initState(): Result<MainPageModel> {
    return MainPageModel().ok()
  }

  override fun getInterface() = this
}