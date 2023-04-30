package org.philblandford.ascore2.features.edit

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

class ToggleBooleanForNotesImpl(private val kScore: KScore) : ToggleBooleanForNotes {

  override fun invoke(param: EventParam, default:Boolean) {
    kScore.getStartSelect()?.let { start ->
      kScore.getEndSelect()?.let { end ->
        val events = kScore.getEvents(EventType.NOTE, start, end)
        val notDefault = events.any { (it.value.getParam<Boolean>(param) ?: default) != default }
        kScore.setParamAtSelection(EventType.NOTE, param, !notDefault)
      }
    }
  }
}