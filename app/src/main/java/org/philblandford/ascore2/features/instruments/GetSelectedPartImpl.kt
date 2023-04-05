package org.philblandford.ascore2.features.instruments

import com.philblandford.kscore.api.KScore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class GetSelectedPartImpl(private val kScore: KScore) : GetSelectedPart{
  private val selectedPartFlow = MutableStateFlow(kScore.getSelectedPart())
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  init {
    coroutineScope.launch {

      kScore.scoreUpdate().collectLatest {
        Timber.e("scoreUpdate ${kScore.getSelectedPart()}")
        selectedPartFlow.emit(kScore.getSelectedPart())
      }
    }
  }

  override operator fun invoke():StateFlow<Int> = selectedPartFlow
}