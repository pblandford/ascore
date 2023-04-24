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

@Suppress("UNCHECKED_CAST")
data class InsertItem(
  val drawable: Int,
  val string: Int,
  val helpTag: String,
  val layoutID: LayoutID,
  val eventType: EventType,
  val params: ParamMap = mapOf(),
  val line: Boolean = false,
  val rangeCapable: Boolean = false,
  val typeParam: EventParam = EventParam.TYPE,
  val tapInsertBehaviour: TapInsertBehaviour = TapInsertBehaviour.INSERT,
  val deleteBehaviour: DeleteBehaviour = DeleteBehaviour.ENTER_STATE,
  val getEventType: (Any) -> EventType = { eventType },
  val isLine: (EventType) -> Boolean = { line },
  val isRangeCapable: (EventType) -> Boolean = {rangeCapable}
) {
  fun <T> getParam(param: EventParam) = params[param] as T?
}
