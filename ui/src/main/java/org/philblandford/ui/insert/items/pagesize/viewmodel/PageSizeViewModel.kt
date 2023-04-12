package org.philblandford.ui.insert.items.pagesize.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.types.PageSize
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.page.GetPageMinMax
import org.philblandford.ascore2.features.page.GetPageWidth
import org.philblandford.ascore2.features.page.SetPageWidth
import org.philblandford.ascore2.features.page.SetPageWidthPreset
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.insert.items.pagesize.model.PageSizeModel

interface PageSizeInterface : VMInterface {
  fun setPageSize(size:Int)
  fun setPreset(preset:PageSize)
}

class PageSizeViewModel(private val getPageWidth: GetPageWidth,
                        private val getPageMinMax: GetPageMinMax,
                        private val setPageWidth: SetPageWidth,
                        private val setPageWidthPreset: SetPageWidthPreset,
) : BaseViewModel<PageSizeModel, PageSizeInterface, VMSideEffect>(), PageSizeInterface {

  init {
    viewModelScope.launch {
      scoreUpdate().collectLatest {
        update { copy(currentSize = getPageWidth()) }
      }
    }
  }

  override suspend fun initState(): Result<PageSizeModel> {
    val (min, max) = getPageMinMax()
    return PageSizeModel(getPageWidth(), min, max, 100).ok()
  }

  override fun getInterface(): PageSizeInterface = this

  override fun setPageSize(size: Int) {
    setPageWidth(size)
  }

  override fun setPreset(preset: PageSize) {
    setPageWidthPreset(preset)
  }
}