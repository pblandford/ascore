package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.LEDGER_OFFSET

data class LedgerDescr(val position: Coord, val width: Int)

fun getLedgerPositions(inputs: List<Coord>, tadpoleWidth: Int): List<LedgerDescr> {
  val grouped =
    inputs.filterNot { it.y > -2 && it.y < 10 }.groupBy { it.y < 0 }
  return grouped.flatMap { (up, coord) -> doGetLedgerPositions(coord, tadpoleWidth, up) }
}

private fun doGetLedgerPositions(
  inputs: List<Coord>,
  tadpoleWidth: Int,
  up: Boolean
): List<LedgerDescr> {
  val sorted = inputs.sortedBy { if (up) it.y else -it.y }
  val positiveCluster = inputs.any { it.x > 0 }
  return if (sorted.isNotEmpty()) {
    val firstCluster = sorted.findFirstCluster(up)
    val firstEven = (sorted.first().y / 2) * 2
    val yPositions = if (up) firstEven..-2 step 2 else firstEven downTo 10 step 2
    yPositions.map { yPos ->
      val tadpoleExtent =
        firstCluster?.let {
          if ((up && yPos > it) || (!up && yPos < it)) tadpoleWidth * 2 else tadpoleWidth
        } ?: tadpoleWidth
      val x = if (!positiveCluster) tadpoleWidth - tadpoleExtent - LEDGER_OFFSET else -LEDGER_OFFSET
      LedgerDescr(Coord(x, yPos * BLOCK_HEIGHT), tadpoleExtent + LEDGER_OFFSET * 2)
    }
  } else {
    listOf()
  }
}

private fun List<Coord>.findFirstCluster(up: Boolean): Int? {
  return windowed(2).find { (one, two) ->
    one.y == if (up) two.y - 1 else two.y + 1
  }?.let { (one, two) ->
    val ret = if (up) one else two
    ret.y
//    if (ret.y % 2 == 0) {
//      ret.y + if (up) 2 else -2
//    } else {
//      ret.y
//    }
  }
}