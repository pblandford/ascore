package org.philblandford.ui.insert.items.ornament.viewmodel

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.OrnamentType
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.items.ornament.model.OrnamentInsertModel
import org.philblandford.ui.insert.row.viewmodel.RowInsertInterface


interface OrnamentInsertInterface : RowInsertInterface<Enum<*>> {
  fun insert(idx: Int)
}

class OrnamentInsertViewModel: InsertViewModel<OrnamentInsertModel, OrnamentInsertInterface>(), OrnamentInsertInterface {

  override suspend fun initState(): Result<OrnamentInsertModel> {
    TODO("Not yet implemented")
  }

  override fun selectItem(idx: Int) {
    TODO("Not yet implemented")
  }

  override fun setParamType(eventParam: EventParam) {
    TODO("Not yet implemented")
  }

  override fun getInterface() = this

  override fun insert(idx: Int) {
    receiveAction { model ->
      when (val item = model.ids[idx].second) {
        is OrnamentType -> {
          updateInsertParams { this + (EventParam.TYPE to item) }
        }
        is EventType -> {
          updateEventType(item)
          updateInsertParams { this - EventParam.TYPE }
        }
      }
      model.ok()
    }
  }
}