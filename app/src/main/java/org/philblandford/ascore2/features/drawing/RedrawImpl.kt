package org.philblandford.ascore2.features.drawing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class RedrawImpl : Redraw, ListenForRedraw {

  private val flow = MutableSharedFlow<List<Int>>()
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  override operator fun invoke(pages:List<Int>) {
    coroutineScope.launch {
      flow.emit(pages)
    }
  }

  override operator fun invoke():SharedFlow<List<Int>> {
    return flow
  }

}