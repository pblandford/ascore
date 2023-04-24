package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.types.BowingType
import com.philblandford.kscore.engine.types.BowingType.*
import com.philblandford.kscore.engine.types.INT_WILD

fun DrawableFactory.bowingArea(bowing: ChordDecoration<BowingType>, upStem: Boolean): Area? {
  return bowing.items.firstOrNull()?.let {
    when (it) {
      DOWN_BOW -> downBowArea(upStem)
      UP_BOW -> upBowArea(upStem)
      LH_PIZZICATO -> lhPizzicatoArea()
      SNAP_PIZZICATO -> pizzicatoArea()
      HARMONIC -> harmonicArea()
    }
  }?.copy(tag = "Bowing")
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