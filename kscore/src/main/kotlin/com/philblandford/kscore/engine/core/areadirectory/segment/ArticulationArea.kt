package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DotArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.ArticulationType.*

fun DrawableFactory.articulationArea(articulations: ChordDecoration<ArticulationType>, chord: Event,
                     above:Boolean): Area? {
  var ordered = articulations.items.sortedBy { ordering[it] }
  if (!chord.isTrue(EventParam.IS_UPSTEM)) ordered = ordered.reversed()

  var main = Area(tag = "Articulation")

  ordered.forEach { articulation ->
    val area = when (articulation) {
      ACCENT -> accentArea()
      STACCATO -> staccatoArea()
      TENUTO -> tenutoArea()
      STACCATISSIMO -> staccatissimoArea(above)
      MARCATO -> marcatoArea(above)
    }
    val gap = if (articulation == ordered.first()) 0 else ARTICULATION_GAP
    area?.let {
      val x = getOffset(articulation)

      main = main.addBelow(
        it.copy(
          event = Event(
            EventType.ARTICULATION,
            paramMapOf(EventParam.TYPE to articulation)
          )
        ), gap, x = x
      )
    }
  }
  return main
}

val ordering = mapOf(
  STACCATO to 0, TENUTO to 1, ACCENT to 2,
  STACCATISSIMO to 3, MARCATO to 4
)

private fun DrawableFactory.accentArea(): Area? {
  return getDrawableArea(
    ImageArgs(
      "articulation_accent",
      INT_WILD,
      ACCENT_HEIGHT
    )
  )?.copy(tag = "Accent")
}

private fun DrawableFactory.staccatoArea(): Area? {
  return getDrawableArea(DotArgs(DOT_WIDTH, DOT_WIDTH))?.copy(tag = "Staccato")
}

private fun DrawableFactory.tenutoArea(): Area? {
  return getDrawableArea(LineArgs(TENUTO_LENGTH, true, TENUTO_THICKNESS))?.copy(tag = "Tenuto")
}

private fun DrawableFactory.marcatoArea(above:Boolean): Area? {
  return getDrawableArea(
    ImageArgs(
      if (above) "articulation_marcato" else "articulation_marcato_up",
      INT_WILD,
      MARCATO_HEIGHT
    )
  )?.copy(tag = "Marcato")
}

private fun DrawableFactory.staccatissimoArea(above:Boolean): Area? {
  return getDrawableArea(ImageArgs(
    if (above) "articulation_staccatissimo" else "articulation_staccatissimo_up",
    INT_WILD, MARCATO_HEIGHT))?.copy(
    tag = "Staccatissimo"
  )
}

private fun getOffset(articulationType: ArticulationType): Int {
  return when (articulationType) {
    ACCENT -> ACCENT_OFFSET
    STACCATO -> STACCCATO_OFFSET
    MARCATO -> MARCATO_OFFSET
    STACCATISSIMO -> STACCATISSIMO_OFFSET
    else -> 0
  }
}