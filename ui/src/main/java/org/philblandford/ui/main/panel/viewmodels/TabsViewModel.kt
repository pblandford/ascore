package org.philblandford.ui.main.panel.viewmodels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.instruments.GetInstruments
import org.philblandford.ascore2.features.instruments.GetSelectedPart
import org.philblandford.ascore2.features.instruments.SelectPart
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import timber.log.Timber

data class TabSelection(
  val full: String,
  val short: String
)

data class TabsModel(
  val short: Boolean,
  val instruments: List<TabSelection>,
  val selected: Int
) : VMModel()

interface TabsInterface : VMInterface {
  fun select(part: Int)
}

class TabsViewModel(
  private val getInstruments: GetInstruments,
  private val getSelectedPart: GetSelectedPart,
  private val selectPart: SelectPart
) : BaseViewModel<TabsModel, TabsInterface, VMSideEffect>(), TabsInterface {

  init {
    viewModelScope.launch {
      getSelectedPart().collectLatest { part ->
        Timber.e("selected $part")
        update { copy(selected = part) }
      }
    }
    viewModelScope.launch {
      scoreUpdate().collectLatest {
        update { copy(instruments = getTabSelections()) }
      }
    }
  }

  override suspend fun initState(): Result<TabsModel> {
    val parts = getTabSelections()
    return TabsModel(true, parts, 0).ok()
  }

  override fun getInterface() = this

  override fun select(part: Int) {
    selectPart(part)
  }

  private fun getTabSelections():List<TabSelection> {
    val selections = listOf(TabSelection("Full", "Full")) + getInstruments().map { TabSelection(it.name, it.abbreviation) }
    return if (selections.size > 2) selections else listOf()
  }
}