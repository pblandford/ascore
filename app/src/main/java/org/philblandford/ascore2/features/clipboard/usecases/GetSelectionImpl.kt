package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.clipboard.entities.Selection

class GetSelectionImpl(private val kScore: KScore) : GetSelection {
  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  private val selectFlow = MutableStateFlow<Selection?>(null)

  init {
    coroutineScope.launch {
      kScore.selectionUpdate().collectLatest {
        val selection = kScore.getStartSelect()?.let { start ->
          val end = kScore.getEndSelect()
          val location = kScore.getSegmentArea(start)?.let { Location(it.page, it.x, it.y) }
          val endLocation =
            end?.let { kScore.getSegmentArea(it) }?.let { Location(it.page, it.x, it.y) }
          Selection(start, end, location, endLocation)
        }

        selectFlow.emit(selection)
      }
    }
  }

  override fun invoke(): StateFlow<Selection?> = selectFlow
}