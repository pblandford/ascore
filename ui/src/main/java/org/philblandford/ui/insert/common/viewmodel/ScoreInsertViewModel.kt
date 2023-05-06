package org.philblandford.ui.insert.common.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.paramMapOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import timber.log.Timber


abstract class ScoreInsertViewModel<M : InsertModel, I : InsertInterface<M>> :
  InsertViewModel<M, I>(), InsertInterface<M> {

  fun listenForUpdates() {

    viewModelScope.launch {
      scoreUpdate().collectLatest {
        Timber.e("ScoreUpdate")
        updateFromScore()
      }
    }
  }

  protected fun updateFromScore() {
    if (getInsertItem()?.eventType in (getExpectedTypes())) {
      val event = updateEvent()
      updateInsertParams { event?.params ?: paramMapOf() }
    }
  }


  abstract fun updateEvent(): Event?
  abstract fun getExpectedTypes():List<EventType>
}