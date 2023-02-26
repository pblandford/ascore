package org.philblandford.ui.insert.items.ornament.viewmodel

import androidx.compose.foundation.layout.Row
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.OrnamentType
import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.usecases.GetInsertItem
import org.philblandford.ascore2.features.ui.usecases.InsertItemMenu
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertEvent
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertParams
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.insert.row.viewmodel.RowInsertInterface
import org.philblandford.ui.insert.row.viewmodel.RowInsertModel
import org.philblandford.ui.insert.row.viewmodel.RowInsertViewModel
import org.philblandford.ui.util.ornamentIds


interface OrnamentInsertInterface : RowInsertInterface<Enum<*>> {
  fun insert(idx: Int)
}

class OrnamentInsertViewModel: RowInsertViewModel<Enum<*>>(ornamentIds), OrnamentInsertInterface {


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