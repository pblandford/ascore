package org.philblandford.ui.layout.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.types.EventParam
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.score.ScoreUpdate
import org.philblandford.ascore2.features.settings.usecases.GetOption
import org.philblandford.ascore2.features.settings.usecases.SetOption
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.R
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.layout.model.LayoutOption
import org.philblandford.ui.layout.model.LayoutOptionModel

interface LayoutOptionInterface : VMInterface {
  fun <T>setOption(param:EventParam, option:T)
  fun toggleOption(param:EventParam)
  fun setNumBars(num:Int)
}

class LayoutOptionViewModel(private val getOption: GetOption,
private val setOptionUC: SetOption) : BaseViewModel<LayoutOptionModel, LayoutOptionInterface, VMSideEffect>(), LayoutOptionInterface {

  init {
    viewModelScope.launch {
      scoreUpdate().collectLatest {
        update { refreshModel() }
      }
    }
  }

  override suspend fun initState(): Result<LayoutOptionModel> {
    return refreshModel().ok()
  }

  private fun refreshModel():LayoutOptionModel {
    var numBars = getOption<Any>(EventParam.OPTION_BARS_PER_LINE) as? Int ?: 4
    if (numBars == 0) numBars = 4
    return LayoutOptionModel(
      numBars, listOf(
        LayoutOption(
          R.string.option_multi_bar, EventParam.OPTION_SHOW_MULTI_BARS,
          getOption<Boolean>(EventParam.OPTION_SHOW_MULTI_BARS)
        ),
        LayoutOption(
          R.string.option_fixed_bars, EventParam.OPTION_BARS_PER_LINE,
          getOption<Any>(EventParam.OPTION_BARS_PER_LINE)
        ),
        LayoutOption(
          R.string.option_transpose, EventParam.OPTION_SHOW_TRANSPOSE_CONCERT,
          getOption<Boolean>(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT)
        ),
        LayoutOption(
          R.string.option_hide_staves, EventParam.OPTION_HIDE_EMPTY_STAVES,
          getOption<Boolean>(EventParam.OPTION_HIDE_EMPTY_STAVES)
        ),
        LayoutOption(
          R.string.option_part_piece, EventParam.OPTION_SHOW_PART_NAME,
          getOption<Boolean>(EventParam.OPTION_SHOW_PART_NAME)
        ),
        LayoutOption(
          R.string.option_part_stave, EventParam.OPTION_SHOW_PART_NAME_START_STAVE,
          getOption<Boolean>(EventParam.OPTION_SHOW_PART_NAME_START_STAVE)
        )
      )
    )
  }


  override fun setNumBars(num: Int) {
    setOptionUC(EventParam.OPTION_BARS_PER_LINE, num)
  }

  override fun getInterface(): LayoutOptionInterface = this

  override fun toggleOption(param: EventParam) {
    (getOption<Boolean>(param))?.let { existing ->
      setOptionUC(param, !existing)
    }
  }

  override fun <T> setOption(param: EventParam, option: T) {
    setOptionUC(param, option)
  }
}