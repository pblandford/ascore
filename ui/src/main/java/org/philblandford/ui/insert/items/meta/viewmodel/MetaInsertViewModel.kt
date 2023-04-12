package org.philblandford.ui.insert.items.meta.viewmodel

import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.paramMapOf
import org.philblandford.ascore2.features.insert.GetFonts
import org.philblandford.ascore2.features.insert.GetMetaEvent
import org.philblandford.ascore2.features.insert.InsertMetaEvent
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertItem
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.ScoreInsertViewModel
import org.philblandford.ui.insert.items.meta.model.MetaInsertModel
import org.philblandford.ui.insert.model.InsertInterface
import timber.log.Timber

interface MetaInsertInterface : InsertInterface<MetaInsertModel> {
  fun insertText(text:String)
}

class MetaInsertViewModel(
  private val insertMetaEvent: InsertMetaEvent,
  private val getMetaEvent: GetMetaEvent,
  private val updateInsertItem: UpdateInsertItem,
  private val getFonts: GetFonts
) :
  ScoreInsertViewModel<MetaInsertModel, MetaInsertInterface>(),
  MetaInsertInterface {

  override suspend fun initState(): Result<MetaInsertModel> {
    setEventType(EventType.TITLE)
    listenForUpdates()
    return MetaInsertModel(getFonts()).ok().onSuccess {
    }
  }

  override fun getInterface() = this

  override fun setEventType(eventType: EventType) {
    val event = getMetaEvent(eventType)
    updateInsertItem{
      copy(eventType = eventType, params = event?.params ?: paramMapOf())
    }
  }

  override fun <T> setParam(key: EventParam, value: T) {
    getInsertItem()?.let {
        insertMetaEvent(it.eventType, it.params + (key to value))
    }
    super.setParam(key, value)
  }


  override fun insertText(text: String) {
    getInsertItem()?.let { item ->
      Timber.e("inserting $item")
      insertMetaEvent(item.eventType,  item.params + (EventParam.TEXT to text))
    }
  }

  override fun updateEvent(): Event? {
    return getInsertItem()?.let { item ->
      Timber.e("got insert item $item")
      getMetaEvent(item.eventType)
    }
  }
}