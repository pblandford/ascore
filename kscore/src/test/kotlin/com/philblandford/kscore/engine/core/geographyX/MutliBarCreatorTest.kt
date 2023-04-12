package com.philblandford.kscore.engine.core.geographyX

import assertEqual
import com.philblandford.kscore.engine.core.BarGeography
import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.types.hZero
import com.philblandford.kscore.engine.types.hz
import org.junit.Test

class MutliBarCreatorTest {

  @Test
  fun testCreateMultiBars() {
    val list = sortedMapOf(1 to empty, 2 to empty)
    val geogs = createMultiBars(list).toList()
    assertEqual(1, geogs.size)
    assertEqual(2, geogs.first().second.numBars)
  }

  @Test
  fun testCreateMultiBarsFullAfter() {
    val list = sortedMapOf(1 to empty, 2 to empty, 3 to full)
    val geogs = createMultiBars(list).toList()
    assertEqual(2, geogs.size)
    assertEqual(2, geogs.first().second.numBars)
    assertEqual(1, geogs.last().second.numBars)
  }

  @Test
  fun testCreateMultiBarsBreaker() {
    val list = sortedMapOf(1 to empty, 2 to empty, 3 to empty, 4 to empty)
    val geogs = createMultiBars(list, setOf(3)).toList()
    assertEqual(2, geogs.size)
    assertEqual(2, geogs.first().second.numBars)
    assertEqual(2, geogs[1].second.numBars)
  }

  private val empty = BarGeography(isEmpty = true)
  private val full  = BarGeography(slicePositions = mapOf(hZero() to SlicePosition(0,0,50)))
}