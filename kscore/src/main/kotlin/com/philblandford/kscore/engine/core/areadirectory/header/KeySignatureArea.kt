package com.philblandford.kscore.engine.core.areadirectory.header

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.pitch.getAccidentalPositions
import com.philblandford.kscore.engine.types.*
import kotlin.math.abs

data class AccSize(
  val id: String,
  val width: Blocks,
  val height: Blocks,
  val yOffset: Blocks,
  val gap: Blocks
)

fun DrawableFactory.keySignatureArea(event: Event): Area? {
  val sharps = event.getInt(EventParam.SHARPS)
  val clef = event.getParam<ClefType>(EventParam.CLEF) ?: ClefType.TREBLE
  val previousSharps = event.getParam<Int>(EventParam.PREVIOUS_SHARPS)

  var area = Area(tag = "KeySignature", event = event)
  val main = createArea(if (sharps > 0) Accidental.SHARP else Accidental.FLAT, sharps, clef)
  main?.let {
    area = area.addArea(it)
  }
  previousSharps?.let { previous ->
    if (sharps == 0) {
      val cancellation = createArea(Accidental.NATURAL, previous, clef)
      cancellation?.let {
        area = area.addArea(it)
      }
    }
  }
  return area
}

private fun DrawableFactory.createArea(accidental: Accidental, sharps: Int, clef: ClefType): Area? {
  val accSize = getAccSize(accidental)
  val accArea =
    getDrawableArea(
      ImageArgs(
        accSize.id,
        INT_WILD,
        accSize.height
      )
    )?.copy(tag = "Accidental-${accidental.toChar(true)}")
      ?: Area()
  var area = Area()
  getAccidentalPositions(clef)?.let { allPositions ->
    (0 until abs(sharps)).forEach {
      val positions = if (sharps > 0) allPositions.sharpPositions else allPositions.flatPositions
      val pos = positions[it]
      val y = (pos * BLOCK_HEIGHT) - accSize.yOffset
      area = area.addRight(accArea, gap = accSize.gap, y = y)
    }
  }
  return area
}

fun getAccSize(accidental: Accidental): AccSize {
  val res = when (accidental) {
    Accidental.SHARP -> AccSize(
      "accidental_sharp",
      SHARP_WIDTH,
      SHARP_HEIGHT,
      SHARP_OFFSET,
      SHARP_GAP
    )
    Accidental.FLAT -> AccSize("accidental_flat", FLAT_WIDTH, FLAT_HEIGHT, FLAT_OFFSET, FLAT_GAP)
    Accidental.DOUBLE_SHARP -> AccSize(
      "accidental_double_sharp",
      DOUBLE_SHARP_WIDTH,
      DOUBLE_SHARP_HEIGHT,
      DOUBLE_SHARP_OFFSET,
      DOUBLE_SHARP_GAP
    )
    Accidental.DOUBLE_FLAT -> AccSize(
      "accidental_double_flat",
      FLAT_WIDTH * 2,
      FLAT_HEIGHT,
      FLAT_OFFSET,
      FLAT_GAP
    )
    Accidental.NATURAL -> AccSize(
      "accidental_natural",
      NATURAL_WIDTH,
      NATURAL_HEIGHT,
      NATURAL_OFFSET,
      NATURAL_GAP
    )
    else -> AccSize("accidental_natural", FLAT_WIDTH, FLAT_HEIGHT, FLAT_OFFSET, FLAT_GAP)
  }
  return res
}