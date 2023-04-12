package org.philblandford.ui.insert.items.pagemargins.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.core.representation.PAGE_BOTTOM_MARGIN
import com.philblandford.kscore.engine.core.representation.PAGE_LEFT_MARGIN
import com.philblandford.kscore.engine.core.representation.PAGE_RIGHT_MARGIN
import com.philblandford.kscore.engine.core.representation.PAGE_TOP_MARGIN
import com.philblandford.kscore.engine.types.EventParam
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.settings.usecases.GetOption
import org.philblandford.ascore2.features.settings.usecases.SetOption
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.R
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.items.pagemargins.model.MarginDescriptor
import org.philblandford.ui.insert.items.pagemargins.model.PageMarginsModel
import org.philblandford.ui.insert.model.InsertInterface

interface PageMarginsInterface : InsertInterface<PageMarginsModel> {
  fun setMargin(eventParam: EventParam, size: Int)
  fun clear()
}

class PageMarginsViewModel(private val getOption: GetOption,
private val setOption: SetOption) :
  InsertViewModel<PageMarginsModel, PageMarginsInterface>(), PageMarginsInterface {

  init {
    viewModelScope.launch {
      scoreUpdate().collectLatest {
        update { getModel() }
      }
    }
  }

  override suspend fun initState(): Result<PageMarginsModel> {
    return getModel().ok()
  }

  override fun getInterface(): PageMarginsInterface = this

  override fun setMargin(eventParam: EventParam, size: Int) {
    setOption(eventParam, size)
  }

  override fun clear() {
    setOption(EventParam.LAYOUT_LEFT_MARGIN, PAGE_LEFT_MARGIN)
    setOption(EventParam.LAYOUT_RIGHT_MARGIN, PAGE_RIGHT_MARGIN)
    setOption(EventParam.LAYOUT_TOP_MARGIN, PAGE_TOP_MARGIN)
    setOption(EventParam.LAYOUT_BOTTOM_MARGIN, PAGE_BOTTOM_MARGIN)
  }

  private fun getModel():PageMarginsModel {
    return PageMarginsModel(
      MarginDescriptor(EventParam.LAYOUT_LEFT_MARGIN, getOption<Int>(EventParam.LAYOUT_LEFT_MARGIN) ?: 0,
       R.string.left_margin),
      MarginDescriptor(EventParam.LAYOUT_RIGHT_MARGIN, getOption<Int>(EventParam.LAYOUT_RIGHT_MARGIN) ?: 0,
        R.string.right_margin),
      MarginDescriptor(EventParam.LAYOUT_TOP_MARGIN, getOption<Int>(EventParam.LAYOUT_TOP_MARGIN) ?: 0,
        R.string.top_margin),
      MarginDescriptor(EventParam.LAYOUT_BOTTOM_MARGIN, getOption<Int>(EventParam.LAYOUT_BOTTOM_MARGIN) ?: 0,
        R.string.bottom_margin),

    )
  }
}