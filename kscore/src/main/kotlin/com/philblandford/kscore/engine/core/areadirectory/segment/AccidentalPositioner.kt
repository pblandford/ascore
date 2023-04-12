package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.representation.Blocks

internal data class AccidentalPositionInput(
  val accidentalInput: AccidentalInput,
  val yPos: Blocks,
  val yAbove: Blocks,
  val yBelow: Blocks,
  val width: Blocks
)

internal fun positionAccidentals(inputs: Iterable<AccidentalPositionInput>): Map<AccidentalInput, Coord> {

  val posMap = mutableMapOf<Int, Iterable<AccidentalPositionInput>>()

  inputs.forEach { aip ->

    val x = getPos(aip, posMap)
    val list = posMap[x] ?: listOf()
    posMap.put(x, list.plus(aip))
  }

  return posMap.flatMap { (k,v) ->
    val x = posMap.size - 1 - k
    v.map { it.accidentalInput to  Coord(x, it.yPos) }
  }.toMap()
}

private fun getPos(
  current: AccidentalPositionInput,
  existing: Map<Int, Iterable<AccidentalPositionInput>>
): Int {

  existing.toSortedMap().forEach { (xPos, aips) ->
    if (!aips.any { clash(current, it) }) {
      return xPos
    }
  }
  return existing.size

}

private fun clash(candidate: AccidentalPositionInput, existing:AccidentalPositionInput):Boolean {
  return existing.yPos + existing.yBelow > candidate.yPos - candidate.yAbove
}