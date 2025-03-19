package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.core.score.offsetLookup
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.duration.semibreve
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.types.ArticulationType
import com.philblandford.kscore.engine.types.ArticulationType.ACCENT
import com.philblandford.kscore.engine.types.ArticulationType.MARCATO
import com.philblandford.kscore.engine.types.ArticulationType.STACCATISSIMO
import com.philblandford.kscore.engine.types.ArticulationType.STACCATO
import com.philblandford.kscore.engine.types.BowingType
import com.philblandford.kscore.engine.types.BowingType.*
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.INT_WILD
import com.philblandford.kscore.engine.types.paramMapOf

fun DrawableFactory.bowingArea(bowing: ChordDecoration<BowingType>,
                               chord:Event,
                               upStem: Boolean): Area? {
  return bowing.items.firstOrNull()?.let {
    val offset = getOffset(it, chord.duration())
    when (it) {
      DOWN_BOW -> downBowArea(upStem)
      UP_BOW -> upBowArea(upStem)
      LH_PIZZICATO -> lhPizzicatoArea()
      SNAP_PIZZICATO -> pizzicatoArea()
      HARMONIC -> harmonicArea()
    }?.let {
      val event = Event(EventType.BOWING, paramMapOf(EventParam.TYPE to it))
      Area(tag = "Bowing", event = event).addRight(it, offset)
    }
  }
}

private fun DrawableFactory.downBowArea(upStem:Boolean): Area? {
  val key = if (upStem) "bowing_down_upstem" else "bowing_down_downstem"
  return getDrawableArea(ImageArgs(key, INT_WILD, DOWNBOW_HEIGHT))
}

private fun DrawableFactory.upBowArea(upStem: Boolean): Area? {
  val key = if (upStem) "bowing_up_upstem" else "bowing_up_downstem"
  return getDrawableArea(ImageArgs(key, INT_WILD, UPBOW_HEIGHT))
}

private fun DrawableFactory.lhPizzicatoArea(): Area? {
  return getDrawableArea(ImageArgs("bowing_lh_pizzicato", INT_WILD, LH_PIZZICATO_HEIGHT))
}

private fun DrawableFactory.pizzicatoArea(): Area? {
  return getDrawableArea(ImageArgs("bowing_pizzicato", INT_WILD, SNAP_PIZZICATO_HEIGHT))
}

private fun DrawableFactory.harmonicArea(): Area? {
  return getDrawableArea(ImageArgs("bowing_harmonic", INT_WILD, HARMONIC_HEIGHT))
}

private fun getOffset(articulationType: BowingType, chordDuration:Duration): Int {
  val x = when (articulationType) {
    DOWN_BOW -> DOWN_BOW_OFFSET
    UP_BOW -> UP_BOW_OFFSET
    LH_PIZZICATO -> LH_PIZZICATO_OFFSET
    SNAP_PIZZICATO -> SNAP_PIZZICATO_OFFSET
    HARMONIC -> HARMONIC_OFFSET
  }
  return if (chordDuration >= semibreve()) {
    x + (BLOCK_HEIGHT * 0.75).toInt()
  } else {
    x
  }
}