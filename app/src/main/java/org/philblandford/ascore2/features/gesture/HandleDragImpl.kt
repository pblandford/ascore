package org.philblandford.ascore2.features.gesture

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.Accidental
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.ui.repository.UiStateRepository
import timber.log.Timber

class HandleDragImpl(
  private val uiStateRepository: UiStateRepository,
  private val kScore: KScore
) : HandleDrag {
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  init {
    coroutineScope.launch {
      uiStateRepository.getDrag().map {
        Timber.e("raw drag $it")
        (it.second / 10).toInt() }
        .stateIn(coroutineScope, SharingStarted.Eagerly, 0).collectLatest { drag ->
        Timber.e("drag $drag")
          if (drag != 0) {
            val musicalShift = if (drag > 0) -1 else 1
            kScore.shiftSelected(
              musicalShift,
              if (musicalShift >= 0) Accidental.SHARP else Accidental.FLAT
            )
          }
      }
    }
  }

  override fun invoke(x: Float, y: Float) {
    uiStateRepository.updateDrag(x, y)
  }
}