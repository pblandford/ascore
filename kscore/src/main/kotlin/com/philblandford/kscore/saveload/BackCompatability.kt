package com.philblandford.kscore.saveload

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.cZero
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.newadder.GenericSubAdder
import com.philblandford.kscore.engine.newadder.rightOrThrow
import com.philblandford.kscore.engine.newadder.scoreDestination
import com.philblandford.kscore.engine.newadder.subadders.MetaSubAdder
import com.philblandford.kscore.engine.newadder.then
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eZero
import com.philblandford.kscore.engine.types.paramMapOf

fun Score.readdMeta(): Score {
  return getEvent(EventType.META, eZero())?.let { meta ->
    MetaSubAdder.addEvent(
      this, scoreDestination, EventType.META,
      meta.params, eZero()
    ).then {
      GenericSubAdder.deleteEvent(it, scoreDestination, EventType.META, paramMapOf(), eZero())
    }.rightOrThrow()
  } ?: this
}

fun Score.setLyricOffset(): Score {
  val coord = getOption<Coord>(EventParam.OPTION_LYRIC_OFFSET) ?: cZero()
  return if (coord != cZero()) {
    GenericSubAdder.setParam(this, scoreDestination, EventType.OPTION, EventParam.OPTION_LYRIC_OFFSET, cZero(), eZero()).
    then {
      GenericSubAdder.setParam(it, scoreDestination, EventType.OPTION, EventParam.OPTION_LYRIC_OFFSET_BY_POSITION,
        listOf(true to 0, false to coord.y), eZero()
      )
    }.rightOrThrow()
  } else this
}
