package com.philblandford.kscore.engine.eventadder.util

import assertEqual
import com.philblandford.kscore.engine.types.Pitch
import com.philblandford.kscore.engine.types.NoteLetter.*
import com.philblandford.kscore.engine.types.Accidental.*
import com.philblandford.kscore.engine.types.ClefType.*
import com.philblandford.kscore.engine.core.area.Coord
import org.junit.Test

class NotePositionerTest {

  @Test
  fun testGetYPosition() {
    assertEqual(0, getYPosition(Pitch(F, NATURAL, 5), TREBLE)?.y)
  }

  @Test
  fun testGetYPositionBass() {
    assertEqual(2, getYPosition(Pitch(F, NATURAL, 3), BASS)?.y)
  }

  @Test
  fun testGetYPosition8va() {
    assertEqual(7, getYPosition(Pitch(F, NATURAL, 5), TREBLE_8VA)?.y)
  }

  @Test
  fun testGetYPosition8vb() {
    assertEqual(0, getYPosition(Pitch(F, NATURAL, 4), TREBLE_8VB)?.y)
  }

  @Test
  fun testGetYPositionBass8va() {
    assertEqual(9, getYPosition(Pitch(F, NATURAL, 3), BASS_8VA)?.y)
  }

  @Test
  fun testGetYPositionBass8vb() {
    assertEqual(-5, getYPosition(Pitch(F, NATURAL, 3), BASS_8VB)?.y)
  }

  @Test
  fun testGetYPositionBSharp() {
    assertEqual(4, getYPosition(Pitch(B, SHARP, 5), TREBLE)?.y)
  }

  @Test
  fun testGetYPositionASharp() {
    assertEqual(5, getYPosition(Pitch(A, SHARP, 4), TREBLE)?.y)
  }

  @Test
  fun testPositionOneNote() {
    val xPos = getXPositions(listOf(Coord(0, 1)), false)
    assertEqual(Coord(0, 1), xPos.first())
  }

  @Test
  fun testPositionTwoNotes() {
    val xPos = getXPositions(listOf(Coord(0, 1), Coord(0, 3)), false)
    assertEqual(Coord(0, 1), xPos.first())
    assertEqual(Coord(0, 3), xPos.last())
  }

  @Test
  fun testPositionTwoCluster() {
    val xPos = getXPositions(listOf(Coord(0, 1), Coord(0, 2)), false)
    assertEqual(Coord(0, 1), xPos.first())
    assertEqual(Coord(-1, 2), xPos.last())
  }

  @Test
  fun testPositionTwoClusterUpstem() {
    val xPos = getXPositions(listOf(Coord(0, 7), Coord(0, 8)), true)
    assertEqual(Coord(1, 7), xPos.first())
    assertEqual(Coord(0, 8), xPos.last())
  }

  @Test
  fun testGetStemDirection() {
    val input = listOf(Coord(0, 2))
    assertEqual(false, getStemDirection(input))
  }

  @Test
  fun testGetStemDirectionOneLowNote() {
    val input = listOf(Coord(0, 4), Coord(0, 16))
    assertEqual(true, getStemDirection(input))
  }

  @Test
  fun testGetXPositions() {
    val input = listOf(Coord(0, 0))
    assertEqual(listOf(Coord(0, 0)), getXPositions(input, true).toList())
  }

  @Test
  fun testGetXPositionsCluster() {
    val input = listOf(Coord(0, 0), Coord(0, 1))
    assertEqual(listOf(Coord(1, 0), Coord(0, 1)).toList(), getXPositions(input, true).toList())
  }

  @Test
  fun testGetXPositionsClusterSameY() {
    val input = listOf(Coord(0, 0), Coord(0, 0))
    assertEqual(listOf(Coord(1, 0), Coord(0, 0)).toList(), getXPositions(input, true).toList())
  }

  @Test
  fun testGetXPositionsFourNotes() {
    val input = listOf(Coord(0, 8), Coord(0, 9), Coord(0, 11), Coord(0, 13))
    assertEqual(
      listOf(Coord(1, 8), Coord(0, 9), Coord(0, 11), Coord(0, 13)).toList(),
      getXPositions(input, true).toList()
    )
  }

  @Test
  fun testGetXPositionsTwoClusters() {
    val input = listOf(Coord(0, 8), Coord(0, 9), Coord(0, 12), Coord(0, 13))
    assertEqual(
      listOf(Coord(1, 8), Coord(0, 9), Coord(1, 12), Coord(0, 13)).toList(),
      getXPositions(input, true).toList()
    )
  }

}