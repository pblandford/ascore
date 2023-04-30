package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Meta
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.*

object MetaSubAdder : BaseEventAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return params.g<Meta>(EventParam.SECTIONS)?.let { meta ->
      if (params.isTrue(EventParam.SIMPLE)) {
        GenericSubAdder.addEvent(score, scoreDestination, eventType, params.minus(EventParam.SIMPLE), eventAddress)
      } else {
        score.fold(meta.sections.toList()) { (type, section) ->
          GenericSubAdder.addEvent(
            this, scoreDestination, type.toEventType(),
            paramMapOf(
              EventParam.TEXT to section.text,
              EventParam.FONT to (section.font ?: ""),
              EventParam.HARD_START to section.offset,
              EventParam.TEXT_SIZE to section.size
            ), eZero()
          )
        }
      }
    } ?: score.ok()
  }
}