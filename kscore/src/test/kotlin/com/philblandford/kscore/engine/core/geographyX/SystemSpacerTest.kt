package core.geographyX

import assertEqual
import com.philblandford.kscore.engine.core.*
import org.junit.Test
import com.philblandford.kscore.engine.core.geographyX.spaceSystems
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.hZero
import java.util.*

class SystemSpacerTest {

  @Test
  fun testSingleLine() {
    val geographies = getGeographies(5, 100)
    val systemGeographies = spaceSystems(
      geographies, mapOf(), mapOf(),
      eventHashOf(), 500
    ).toList()
    assertEqual(1, systemGeographies.size)
    assertEqual(
      listOf(0, 100, 200, 300, 400).toList(),
      systemGeographies.first().barPositions.toList().map { it.second.pos })
  }

  @Test
  fun testTwoLines() {
    val geographies = getGeographies(10, 100)
    val systemGeographies = spaceSystems(
      geographies, mapOf(), mapOf(),
      eventHashOf(), 500
    ).toList()
    assertEqual(2, systemGeographies.size)
    assertEqual(
      listOf(0, 100, 200, 300, 400).toList(),
      systemGeographies.first().barPositions.toList().map { it.second.pos })
    assertEqual(
      listOf(0, 100, 200, 300, 400).toList(),
      systemGeographies.last().barPositions.toList().map { it.second.pos })
  }


  private fun getGeographies(num: Int, width: Int): SortedMap<Int, BarGeography> {
    val list = (1..num).map {
      it to BarGeography(
        BarStartGeographyPair(), BarEndGeographyPair(),
        mapOf(hZero() to SlicePosition(0, 0, 100))
      )
    }
    return sortedMapOf(*list.toTypedArray())
  }
}