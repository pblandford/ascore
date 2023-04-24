package org.philblandford.ui.main.inputpage.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.api.Location
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.clipboard.usecases.GetSelection
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr
import org.philblandford.ascore2.features.crosscutting.usecases.GetError
import org.philblandford.ascore2.features.error.GetErrorFlow
import org.philblandford.ascore2.features.instruments.GetInstruments
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.usecases.GetHelpKey
import org.philblandford.ascore2.features.ui.usecases.GetUIState
import org.philblandford.ascore2.features.ui.usecases.SetHelpKey
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import timber.log.Timber

interface MainPageInterface : VMInterface {
  fun dismissHelp()
  fun toggleVertical()
}

data class MainPageModel(
  val showClipboard: Boolean = false,
  val showNoteZoom:Boolean = false,
  val showEdit: Boolean = false,
  val vertical:Boolean = true,
  val canShowTabs:Boolean = false,
  val selectedArea: Location? = null,
  val helpKey:String? = null
) : VMModel()

sealed class MainPageSideEffect : VMSideEffect() {
  data class Error(val errorDescr: ErrorDescr) : MainPageSideEffect()
}

class MainPageViewModel(
  getUIState: GetUIState,
  private val getError: GetErrorFlow,
  getSelection: GetSelection,
  private val getHelpKey: GetHelpKey,
  private val setHelpKey: SetHelpKey,
  private val getInstruments: GetInstruments
) :
  BaseViewModel<MainPageModel, MainPageInterface, MainPageSideEffect>(), MainPageInterface {

  init {
    getSelection().combine(getUIState()) { selection, uiState ->
      update {
        copy(
          showClipboard = uiState == UIState.Clipboard,
          showNoteZoom = uiState is UIState.MoveNote,
          showEdit = uiState is UIState.Edit,
          selectedArea = selection?.startLocation
        )
      }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Unit)

    viewModelScope.launch {
      getError().collectLatest { error ->
        Timber.e("Error $error")
        launchEffect(
          MainPageSideEffect.Error(
            ErrorDescr(
              error.exception.message ?: "",
              error.command.toString()
            )
          )
        )
      }
    }

    viewModelScope.launch {
      getHelpKey().collectLatest { key ->
        update { copy(helpKey = key) }
      }
    }

    viewModelScope.launch {
      scoreUpdate().map { getInstruments().size }.distinctUntilChanged().collectLatest { numInstruments ->
        Timber.e("HEY! $numInstruments ${numInstruments > 1}")
        update { copy(canShowTabs = numInstruments > 1) }
      }
    }
  }

  override suspend fun initState(): Result<MainPageModel> {
    return MainPageModel(canShowTabs = getInstruments().size > 1).ok()
  }

  override fun getInterface() = this

  override fun dismissHelp() {
    setHelpKey(null)
  }

  override fun toggleVertical() {
    update { copy(vertical = !vertical) }
  }
}