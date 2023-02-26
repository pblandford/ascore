package org.philblandford.ui.main.inputpage.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
  val showClipboard: Boolean = false
) : VMModel()

sealed class MainPageSideEffect : VMSideEffect() {
  data class Error(val errorDescr: ErrorDescr) : MainPageSideEffect()
}

class MainPageViewModel(private val getUIState: GetUIState,
private val getError: GetError) :
  BaseViewModel<MainPageModel, VMInterface, MainPageSideEffect>(), VMInterface {

  init {
    viewModelScope.launch {
      getUIState().collectLatest { uiState ->
        Timber.e("Latest state $uiState")
        update { copy(showClipboard = uiState == UIState.Clipboard) }
      }
    }
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