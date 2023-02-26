package org.philblandford.ascore2.features.ui.model

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap

enum class TapInsertBehaviour {
  INSERT, SET_MARKER, NONE
}

enum class DeleteBehaviour {
  ENTER_STATE, DELETE_AT_MARKER
}

data class InsertItem(
  val drawable: Int,
  val string: Int,
  val helpTag: String,
  val layoutID: LayoutID,
  val eventType: EventType,
  val params:ParamMap = mapOf(),
  val line: Boolean = false,
  val rangeCapable: Boolean = false,
  val tapInsertBehaviour: TapInsertBehaviour = TapInsertBehaviour.INSERT,
  val deleteBehaviour: DeleteBehaviour = DeleteBehaviour.ENTER_STATE,
) {
  fun <T>getParam(param:EventParam)= params[param] as T
}

val stubItem = InsertItem(-1, -1, "", LayoutID.LAYOUT, EventType.NO_TYPE)
