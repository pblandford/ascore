package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DiagonalArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.LINE_THICKNESS
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.util.highestBit

fun DrawableFactory.tremoloArea(event: Event): Area? {
  return event.getParam<ChordDecoration<Duration>>(EventParam.TREMOLO_BEATS)?.let { beats ->
    val numStrokes = beats.items.first().denominator.highestBit() - 3
    getDrawableArea(
      DiagonalArgs(
        BLOCK_HEIGHT * 3, BLOCK_HEIGHT * 2,
        LINE_THICKNESS * 2, true
      )
    )?.copy(tag = "TremoloStroke")?.let { stroke ->
      var area = Area(tag = "Tremolo", event = event)
      repeat(numStrokes) {
        area = area.addArea(stroke, Coord(0, it * LINE_THICKNESS*4))
      }
      area
    }
  }
}