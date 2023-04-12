package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.api.DrawableGetter
import com.philblandford.kscore.engine.accidental.mapAccidentals
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.areadirectory.header.clefArea
import com.philblandford.kscore.engine.core.areadirectory.header.headerArea
import com.philblandford.kscore.engine.core.areadirectory.segment.SegmentArea
import com.philblandford.kscore.engine.core.areadirectory.segment.segmentArea
import com.philblandford.kscore.engine.core.stave.staveLinesArea
import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.newadder.util.setAllPositions
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*

class Standalone(private val area: Area, private val drawableGetter: DrawableGetter) {

  val width = area.width
  val height = area.height

  fun draw(vararg args: Any) {
    drawableGetter.prepare(*args)
    drawableGetter.drawTree(area)
  }
}

class StandaloneGenerator(private val drawableGetter: DrawableGetter) {

  val drawableFactory = DrawableFactory(drawableGetter)

  fun getHeader(clef: ClefType? = null, ks: Int? = null, ts: TimeSignature? = null): Standalone? {
    var map = mapOf<EventType, Event>()
    clef?.let {
      map = map.plus(EventType.CLEF to Event(EventType.CLEF, paramMapOf(EventParam.TYPE to it)))
    }
    ks?.let {
      map = map.plus(EventType.KEY_SIGNATURE to Event(EventType.KEY_SIGNATURE, paramMapOf(EventParam.SHARPS to it)))
    }
    ts?.let {
      map = map.plus(EventType.TIME_SIGNATURE to ts.toEvent())
    }
    return drawableFactory.headerArea(map)?.let { area ->
      drawableFactory.staveLinesArea(area.base.width)?.let { staveLines ->
        Standalone(area.base.addArea(staveLines), drawableGetter)
      }
    }
  }

  fun getChord(chord: Chord, clefType: ClefType = ClefType.TREBLE):Standalone? {
    val chordEvent = setAllPositions(chord.toEvent(), clefType)
    return drawableFactory.segmentArea(mapOf(EMK(EventType.DURATION, eZero()) to chordEvent), eZero())?.let { sa ->
      drawableFactory.wrapper(clefType, sa.base)?.let { wrapper ->
        Standalone(wrapper, drawableGetter)
      }
    }
  }

  fun getChords(chords:List<Chord>, clefType: ClefType):Standalone? {
    val areas = chords.mapNotNull { chord ->
      val chordEvent = setAllPositions(chord.toEvent(), clefType)
      drawableFactory.segmentArea(mapOf(EMK(EventType.DURATION, eZero()) to chordEvent), eZero())
    }
    val area = areas.fold(Area()) { base, a ->
      base.addRight(a.base, BLOCK_HEIGHT + a.base.xMargin)
    }
    return drawableFactory.wrapper(clefType, area)?.let { wrapper ->
      Standalone(wrapper, drawableGetter)
    }
  }

  fun getText(string:String, font:String):Standalone? {
    return drawableFactory.getDrawableArea(TextArgs(string, font = font))?.let {
        Standalone(it, drawableGetter)
    }
  }

  private fun DrawableFactory.wrapper(clefType: ClefType = ClefType.TREBLE, content:Area):Area? {
    return clefArea(Event(EventType.CLEF, paramMapOf(EventParam.TYPE to clefType)))?.let { clefArea ->
      drawableFactory.staveLinesArea(clefArea.width + content.width + BLOCK_HEIGHT*3)?.let { staveLines ->
        Area().addArea(clefArea).addRight(content, BLOCK_HEIGHT * 2 + content.xMargin).addArea(staveLines)
      }
    }
  }
}