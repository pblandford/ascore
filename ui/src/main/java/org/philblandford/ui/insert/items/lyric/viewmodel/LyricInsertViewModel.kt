package org.philblandford.ui.insert.items.lyric.viewmodel

import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import kotlinx.coroutines.flow.map
import org.philblandford.ascore2.features.input.usecases.MoveMarker
import org.philblandford.ascore2.features.insert.InsertEventAtMarker
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.usecases.GetInsertItem
import org.philblandford.ascore2.features.ui.usecases.InsertItemMenu
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertEvent
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertParams
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.items.lyric.model.LyricInsertModel
import org.philblandford.ui.insert.model.InsertInterface

interface LyricInsertInterface : InsertInterface<LyricInsertModel> {
  fun insertLyric(text: String)
  fun nextSyllable()
  fun markerLeft()
  fun markerRight()
}

class LyricInsertViewModel(
  private val moveMarker: MoveMarker,
  private val insertEventAtMarker: InsertEventAtMarker
) : InsertViewModel<LyricInsertModel, LyricInsertInterface>(), LyricInsertInterface {

  override suspend fun initState(): Result<LyricInsertModel> {
    return LyricInsertModel().ok()
  }

  override fun getInterface(): LyricInsertInterface {
    return this
  }

  override fun insertLyric(text: String) {

    receiveAction { model ->
      if (text.lastOrNull() == ' ') {
        moveMarker(false)
        updateInsertParams { this + (EventParam.TEXT to "") }
        model.ok()
      } else {
        updateInsertParams { this + (EventParam.TEXT to text) }
        getInsertState().value.insertItem?.let {
          insertEventAtMarker(Event(EventType.LYRIC, it.params)).map { model }
        } ?: model.ok()
      }
    }
  }


  override fun nextSyllable() {
    getInsertItem()?.let { insertItem ->
      moveMarker(false)
      updateInsertParams { insertItem.params + (EventParam.TEXT to (insertItem.getParam<String>(EventParam.TEXT) + " -")) }
    }
  }

  override fun markerLeft() {
    moveMarker(true)
  }

  override fun markerRight() {
    moveMarker(false)
  }
}