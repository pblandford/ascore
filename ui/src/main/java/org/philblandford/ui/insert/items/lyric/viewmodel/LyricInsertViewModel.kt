package org.philblandford.ui.insert.items.lyric.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.paramMapOf
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.input.usecases.MoveMarker
import org.philblandford.ascore2.features.insert.GetLyricAtMarker
import org.philblandford.ascore2.features.insert.GetMarker
import org.philblandford.ascore2.features.insert.InsertEventAtMarker
import org.philblandford.ascore2.features.insert.InsertLyricAtMarker
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.insert.common.viewmodel.ScoreInsertViewModel
import org.philblandford.ui.insert.items.lyric.model.LyricInsertModel
import org.philblandford.ui.insert.model.InsertInterface
import timber.log.Timber

interface LyricInsertInterface : InsertInterface<LyricInsertModel> {
  fun insertLyric(text: String)
  fun nextSyllable()
  fun markerLeft()
  fun markerRight()
  fun setNumber(number: Int)
}

sealed class LyricInsertSideEffect : VMSideEffect() {
  object UpdateText : VMSideEffect()
}

class LyricInsertViewModel(
  private val moveMarker: MoveMarker,
  private val insertLyricAtMarker: InsertLyricAtMarker,
  private val getLyricAtMarker: GetLyricAtMarker,
  private val getMarker: GetMarker
) : ScoreInsertViewModel<LyricInsertModel, LyricInsertInterface>(), LyricInsertInterface {

  private var markerPosition = scoreUpdate().map { getMarker() }

  override suspend fun initState(): Result<LyricInsertModel> {
    listenForUpdates()
    viewModelScope.launch {
      markerPosition.stateIn(viewModelScope).collectLatest {
        launchEffect(LyricInsertSideEffect.UpdateText)
      }
    }
    return LyricInsertModel().ok()
  }

  override fun getInterface(): LyricInsertInterface {
    return this
  }

  override fun insertLyric(text: String) {

    Timber.e("insertLyric $text")

    receiveAction { model ->
      if (text.lastOrNull() == ' ') {
        moveMarker(false)
      } else {
        insertLyricAtMarker(text, model.number)
      }
      model.ok()
    }
  }


  override fun nextSyllable() {
    getInsertItem()?.let { insertItem ->
      val text = insertItem.getParam<String>(EventParam.TEXT) + " -"
      insertLyricAtMarker(text, getState().value?.number ?: 1)
      moveMarker(false)
    }
  }

  override fun markerLeft() {
    moveMarker(true)
  }

  override fun markerRight() {
    moveMarker(false)
  }

  override fun updateEvent(): Event? {
    return getLyricAtMarker(getState().value?.number ?: 1)?.let {
      Timber.e("getLyric $it")
      Event(
        EventType.LYRIC,
        paramMapOf(EventParam.TEXT to it)
      )
    }
  }

  override fun setNumber(number: Int) {
    updateSynchronous { copy(number = number.coerceIn(1, maxNum)) }
    updateFromScore()
  }

}