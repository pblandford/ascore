package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.areadirectory.header.getAccSize
import com.philblandford.kscore.engine.core.representation.ORNAMENT_ACCIDENTAL_GAP
import com.philblandford.kscore.engine.core.representation.ORNAMENT_HEIGHT
import com.philblandford.kscore.engine.newadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.types.*

fun DrawableFactory.ornamentArea(ornamentType: ChordDecoration<Ornament>, event: Event): Area? {
  return ornamentType.items.firstOrNull()?.let { ornament ->
    var main = Area(
      tag = "Ornament", event = ornament.toEvent(),
      addressRequirement = AddressRequirement.EVENT
    )
    val key = when (ornament.ornamentType) {
      OrnamentType.TRILL -> "ornament_trill"
      OrnamentType.TURN -> "ornament_turn"
      OrnamentType.MORDENT -> "ornament_mordent"
      OrnamentType.LOWER_MORDENT -> "ornament_lower_mordent"
    }

    getDrawableArea(ImageArgs(key, INT_WILD, ORNAMENT_HEIGHT))?.let {
      main = main.addArea(it)
    }
    main = addAccidentalArea(main, ornament.accidentalAbove, true)
    main = addAccidentalArea(main, ornament.accidentalBelow, false)
    main
  }
}

private fun DrawableFactory.addAccidentalArea(main: Area, accidental: Accidental?, above: Boolean): Area {
  return accidental?.let { ornamentAccidental(it) }?.let {
    val xPos = main.width / 2 - it.width / 2
    return if (above) {
      main.addAbove(it, ORNAMENT_ACCIDENTAL_GAP, xPos, shiftDown = true)
    } else {
      main.addBelow(it, ORNAMENT_ACCIDENTAL_GAP, xPos)
    }
  } ?: main

}

internal fun DrawableFactory.ornamentAccidental(accidental: Accidental): Area? {
  val accSize = getAccSize(accidental)
  return getDrawableArea(
    ImageArgs(
      accSize.id,
      INT_WILD,
      (accSize.height * 0.6f).toInt()
    )
  )?.copy(tag = "OrnamentAccidental")
}