package org.philblandford.ui.insert.items.meta.viewmodel

import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.paramMapOf
import org.philblandford.ascore2.features.insert.GetMetaEvent
import org.philblandford.ascore2.features.insert.InsertMetaEvent
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertItem
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.ScoreInsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import timber.log.Timber

interface MetaInsertInterface : InsertInterface<InsertModel> {
  fun insertText(text:String)
}

class MetaInsertViewModel(
  private val insertMetaEvent: InsertMetaEvent,
  private val getMetaEvent: GetMetaEvent,
  private val updateInsertItem: UpdateInsertItem
) :
  ScoreInsertViewModel<InsertModel, MetaInsertInterface>(),
  MetaInsertInterface {


  override suspend fun initState(): Result<InsertModel> {
    setEventType(EventType.TITLE)
    listenForUpdates()
    return InsertModel().ok().onSuccess {
    }
  }

  override fun getInterface() = this

  override fun setEventType(eventType: EventType) {
    val text = getMetaEvent(eventType)
    updateInsertItem{
      copy(eventType = eventType, params = params + (EventParam.TEXT to text))
    }
  }

  override fun <T> setParam(key: EventParam, value: T) {
    getInsertItem()?.let {
      (value as? String)?.let { string ->
        insertMetaEvent(it.eventType, string)
      }
    }
    super.setParam(key, value)
  }


  override fun insertText(text: String) {
    getInsertItem()?.let { item ->
      Timber.e("inserting $item")
      insertMetaEvent(item.eventType,  text)
    }
  }

  override fun updateEvent(): Event? {
    return getInsertItem()?.let { item ->
      Timber.e("got insert item $item")
      val text = getMetaEvent(item.eventType)
      Timber.e("text $text")
      Event(item.eventType, paramMapOf(EventParam.TEXT to text))
    }
  }
}