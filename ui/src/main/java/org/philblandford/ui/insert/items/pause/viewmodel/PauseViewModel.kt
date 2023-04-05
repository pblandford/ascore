package org.philblandford.ui.insert.items.pause.viewmodel

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.FermataType
import org.philblandford.ui.insert.row.viewmodel.RowInsertViewModel
import org.philblandford.ui.util.fermataIds
import org.philblandford.ui.util.pauseIds

class PauseViewModel : RowInsertViewModel<Any>(fermataIds + pauseIds) {

  override fun <T> setParam(key: EventParam, value: T) {
    super.setParam(key, value)
    if (key == EventParam.TYPE) {
      when (value) {
        is FermataType -> setEventType(EventType.FERMATA)
        else -> setEventType(EventType.PAUSE)
      }
    }
  }
}