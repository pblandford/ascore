package org.philblandford.ui.insert.items.segmentwidth.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.page.GetSegmentMinMax
import org.philblandford.ascore2.features.page.GetSegmentWidth
import org.philblandford.ascore2.features.page.SetSegmentWidth
import org.philblandford.ascore2.features.score.ScoreUpdate
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.insert.items.segmentwidth.model.SegmentWidthModel

interface SegmentWidthInterface : VMInterface {
  fun setSegmentWidth(width: Int)
  fun clearSegmentWidth()
}

class SegmentWidthViewModel(
  private val getSegmentMinMax: GetSegmentMinMax,
  private val getSegmentWidth: GetSegmentWidth,
  private val setSegmentWidthUC: SetSegmentWidth,
) : BaseViewModel<SegmentWidthModel, SegmentWidthInterface, VMSideEffect>(), SegmentWidthInterface {

  init {
    viewModelScope.launch {
      scoreUpdate().collectLatest {
        update { copy(current = getSegmentWidth()) }
      }
    }
  }

  override suspend fun initState(): Result<SegmentWidthModel> {
    val (min, max) = getSegmentMinMax()
    return SegmentWidthModel(getSegmentWidth(), min, max).ok()
  }

  override fun getInterface(): SegmentWidthInterface = this

  override fun setSegmentWidth(width: Int) {
    setSegmentWidthUC(width)
  }

  override fun clearSegmentWidth() {
    setSegmentWidthUC(-1)
  }
}