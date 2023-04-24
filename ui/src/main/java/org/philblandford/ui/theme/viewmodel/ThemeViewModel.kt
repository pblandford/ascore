package org.philblandford.ui.theme.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.settings.usecases.GetColors
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.theme.model.ThemeModel

class ThemeViewModel(private val getColors: GetColors) : BaseViewModel<ThemeModel, VMInterface, VMSideEffect>(), VMInterface {

  init {
    viewModelScope.launch {
      getColors().collectLatest { colors ->
        update { copy(colorScheme = colors) }
      }
    }
  }

  override suspend fun initState(): Result<ThemeModel> {
    return ThemeModel(getColors().value).ok()
  }

  override fun getInterface() = this
}