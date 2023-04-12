package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.areadirectory.header.getAccSize
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.Blocks
import com.philblandford.kscore.engine.core.representation.SHARP_GAP
import com.philblandford.kscore.engine.core.representation.SHARP_WIDTH
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.INT_WILD
import com.philblandford.kscore.engine.types.eZero

internal data class AccidentalInput(val pos: Blocks, val accidental: Accidental)

internal fun DrawableFactory.accidentalArea(inputs: Iterable<AccidentalInput>): Area {
  var area = Area(tag = "AccidentalArea")
  val positions = getPositions(inputs)
  inputs.withIndex().forEach { iv ->
    val pair = accidental(iv.value.accidental)
    val xPos = (positions[iv.value]?.x ?: 0) * (SHARP_WIDTH + SHARP_GAP)
    area = area.addArea(pair.first, Coord(xPos, iv.value.pos * BLOCK_HEIGHT - pair.second),
      eZero().copy(id = iv.index+1))
      .extendRight(SHARP_GAP)
  }
  return area
}

private fun getPositions(inputs:Iterable<AccidentalInput>):Map<AccidentalInput, Coord> {

  val positionInputs = inputs.map { ai ->
    val accSize = getAccSize(ai.accidental)
    AccidentalPositionInput(ai, ai.pos* BLOCK_HEIGHT, accSize.yOffset,
      accSize.height - accSize.yOffset, accSize.width)
  }
  return positionAccidentals(positionInputs)
}

private fun DrawableFactory.accidental(accidental: Accidental): Pair<Area, Int> {
  val accSize = getAccSize(accidental)
  val area = getDrawableArea(ImageArgs(accSize.id, INT_WILD, accSize.height)) ?: Area()
  return Pair(area.copy(tag = "Accidental"), accSize.yOffset)
}