package com.philblandford.kscore.engine.core.areadirectory

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.areadirectory.segment.AccidentalInput
import com.philblandford.kscore.engine.core.areadirectory.segment.AccidentalPositionInput
import com.philblandford.kscore.engine.core.areadirectory.segment.positionAccidentals
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.types.Accidental
import org.junit.Test

class AccidentalPositionerTest {

  @Test
  fun testPositionOne() {
    val res = positionAccidentals(listOf(AIP(0, 3, 3, 2)))
    assertEqual(listOf(Coord(0, 0)).toList(), res.values.toList())
  }


  @Test
  fun testPositionTwoClustered() {
    val res = positionAccidentals(
      listOf(
        AIP(0, 3, 3, 2),
        AIP(1, 3, 3, 2)
      )
    )
    assertEqual(listOf(Coord(1, 0), Coord(0, 1)).toList(), res.values.toList())
  }

  @Test
  fun testPositionTwoNotClustered() {
    val res = positionAccidentals(
      listOf(
        AIP(0, 3, 3, 2),
        AIP(7, 3, 3, 2)
      )
    )
    assertEqual(listOf(Coord(0, 0), Coord(0, 7)).toList(), res.values.toList())
  }

  @Test
  fun testPositionTwoClusteredThenAnother() {
    val res = positionAccidentals(
      listOf(
        AIP(0, 3, 3, 2),
        AIP(1, 3, 3, 2),
        AIP(9, 3, 3, 2)
      )
    )
    assertEqual(listOf(Coord(1, 0), Coord(0, 1), Coord(1,9)).toList(), res.values.sortedBy { it.y }.toList())
  }


  private fun AIP(
    positionBlocks: Int,
    yAboveBlocks: Int,
    yBelowBlocks: Int,
    widthBlocks: Int
  ): AccidentalPositionInput {
    return AccidentalPositionInput(
      AccidentalInput(positionBlocks, Accidental.SHARP),
      positionBlocks,
      yAboveBlocks,
      yBelowBlocks,
      widthBlocks
    )
  }
}