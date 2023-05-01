package org.philblandford.ui.main.inputpage.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.api.Location
import com.philblandford.kscore.engine.types.EventAddress
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
import org.philblandford.ascore2.features.scorelayout.usecases.GetScoreLayout
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
  val selectedArea: EventAddress? = null,
  val helpKey:String? = null,
  val showScrollType:Boolean = false
) : VMModel()

sealed class MainPageSideEffect : VMSideEffect() {
  data class Error(val errorDescr: ErrorDescr) : MainPageSideEffect()
}

class MainPageViewModel(
  getUIState: GetUIState,
  private val getError: GetError,
  getSelection: GetSelection,
  private val getHelpKey: GetHelpKey,
  private val setHelpKey: SetHelpKey,
  private val getInstruments: GetInstruments,
  private val getScoreLayout: GetScoreLayout
) :
  BaseViewModel<MainPageModel, MainPageInterface, MainPageSideEffect>(), MainPageInterface {

  init {
    getSelection().combine(getUIState()) { selection, uiState ->
      update {
        copy(
          showClipboard = uiState == UIState.Clipboard,
          showNoteZoom = uiState is UIState.MoveNote,
          showEdit = uiState is UIState.Edit,
          selectedArea = selection?.start,
        )
      }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Unit)

    viewModelScope.launch {
      getError().collectLatest { error ->
        Timber.e("Error $error")
        launchEffect(
          MainPageSideEffect.Error(ErrorDescr("An internal error has occured",
          "Apologies, a report has been sent to the developer"))
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
        update { copy(canShowTabs = numInstruments > 1, showScrollType = getScoreLayout().numPages > 1) }
      }
    }
  }

  override suspend fun initState(): Result<MainPageModel> {
    return MainPageModel(canShowTabs = getInstruments().size > 1,
      showScrollType = getScoreLayout().numPages > 1
    ).ok()
  }

  override fun getInterface() = this

  override fun dismissHelp() {
    setHelpKey(null)
  }

  override fun toggleVertical() {
    update { copy(vertical = !vertical) }
  }
}