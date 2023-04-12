package com.philblandford.kscore.engine.core.area

import assertEqual
import assertListEqual
import com.philblandford.kscore.engine.core.areadirectory.segment.LedgerDescr
import com.philblandford.kscore.engine.core.areadirectory.segment.getLedgerPositions
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.LEDGER_OFFSET
import com.philblandford.kscore.engine.core.representation.TADPOLE_WIDTH
import org.junit.Test

class LedgerMapperTest {

  private val EXP_WIDTH = TADPOLE_WIDTH + LEDGER_OFFSET * 2

  @Test
  fun testCreateOneLedger() {
    val positions = listOf(Coord(0, -2))
    val output = getLedgerPositions(positions, TADPOLE_WIDTH)
    assertEqual(
      listOf(
        LedgerDescr(
          Coord(-LEDGER_OFFSET, -2 * BLOCK_HEIGHT),
          EXP_WIDTH
        )
      ).toList(), output.toList()
    )
  }

  @Test
  fun testCreateTwoLedgers() {
    val positions = listOf(
      Coord(0, -4),
      Coord(0, -2)
    )
    val output = getLedgerPositions(positions, TADPOLE_WIDTH)
    assertListEqual(
      listOf(
        LedgerDescr(Coord(-LEDGER_OFFSET, -4 * BLOCK_HEIGHT), EXP_WIDTH),
        LedgerDescr(Coord(-LEDGER_OFFSET, -2 * BLOCK_HEIGHT), EXP_WIDTH)
      ).toList(), output
    )
  }

  @Test
  fun testNoteWithinStaffNotCreated() {
    val positions = listOf(Coord(0, 0))
    val output = getLedgerPositions(positions, TADPOLE_WIDTH)
    assert(output.isEmpty())
  }

  @Test
  fun testCreateTwoLedgersClustered() {
    val positions = listOf(
      Coord(-TADPOLE_WIDTH, -3),
      Coord(0, -2)
    )
    val output = getLedgerPositions(positions, TADPOLE_WIDTH)
    assertListEqual(
      listOf(
        LedgerDescr(
          Coord(-LEDGER_OFFSET - TADPOLE_WIDTH, -2 * BLOCK_HEIGHT),
          TADPOLE_WIDTH * 2 + LEDGER_OFFSET * 2
        )
      ), output
    )
  }

  @Test
  fun testCreateTwoLedgersClusteredFirstNoteEven() {
    val positions = listOf(
      Coord(-TADPOLE_WIDTH, -4),
      Coord(0, -3)
    )
    val output = getLedgerPositions(positions, TADPOLE_WIDTH)
    assertListEqual(
      listOf(
        LedgerDescr(
          Coord(-LEDGER_OFFSET, -4 * BLOCK_HEIGHT),
          TADPOLE_WIDTH + LEDGER_OFFSET * 2
        ),
        LedgerDescr(
          Coord(-LEDGER_OFFSET - TADPOLE_WIDTH, -2 * BLOCK_HEIGHT),
          TADPOLE_WIDTH * 2 + LEDGER_OFFSET * 2
        )
      ), output
    )
  }


  @Test
  fun testCreateOneLedgerBelow() {
    val positions = listOf(Coord(0, 10))
    val output = getLedgerPositions(positions, TADPOLE_WIDTH)
    assertEqual(
      listOf(
        LedgerDescr(
          Coord(-LEDGER_OFFSET, 10 * BLOCK_HEIGHT),
          EXP_WIDTH
        )
      ).toList(), output.toList()
    )
  }

  @Test
  fun testCreateClusterBelow() {
    val positions = listOf(Coord(1, 11), Coord(0, 12))
    val output = getLedgerPositions(positions, TADPOLE_WIDTH)
    assertListEqual(
      listOf(
        LedgerDescr(
          Coord(-LEDGER_OFFSET, 12 * BLOCK_HEIGHT),
          EXP_WIDTH
        ),
        LedgerDescr(
          Coord(-LEDGER_OFFSET, 10 * BLOCK_HEIGHT),
          EXP_WIDTH + TADPOLE_WIDTH
        )
      ), output
    )
  }

  @Test
  fun testCreateTwoClustersBelowGap() {
    val positions = listOf(Coord(1, 10), Coord(0, 11), Coord(1, 14), Coord(0, 15))
    val output = getLedgerPositions(positions, TADPOLE_WIDTH)
    assertListEqual(
      listOf(
        LedgerDescr(
          Coord(-LEDGER_OFFSET, 10 * BLOCK_HEIGHT),
          EXP_WIDTH + TADPOLE_WIDTH
        ),
        LedgerDescr(
          Coord(-LEDGER_OFFSET, 12 * BLOCK_HEIGHT),
          EXP_WIDTH + TADPOLE_WIDTH
        ),
        LedgerDescr(
          Coord(-LEDGER_OFFSET, 14 * BLOCK_HEIGHT),
          EXP_WIDTH
        )
      ), output.sortedBy { it.position.y }
    )
  }
}