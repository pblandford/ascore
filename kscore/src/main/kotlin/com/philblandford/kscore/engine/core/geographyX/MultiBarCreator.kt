package com.philblandford.kscore.engine.core.geographyX

import com.philblandford.kscore.engine.core.BarGeography
import java.util.*

fun createMultiBars(geogs:SortedMap<Int, BarGeography>,
                    breakers:Set<Int> = setOf()):SortedMap<Int, BarGeography> {

  val newGeogs = mutableMapOf<Int, BarGeography>()
  var currentMultiBar:Pair<Int,BarGeography>? = null

  geogs.forEach { (bar, geog) ->
    if (breakers.contains(bar)) {
      currentMultiBar?.let { newGeogs[it.first] = it.second; currentMultiBar = null }
    }

    if (geog.isEmpty) {
      currentMultiBar =
        currentMultiBar?.let { it.first to it.second.copy(numBars = it.second.numBars + 1) }
          ?: (bar to BarGeography(numBars = 1))
    } else {
      currentMultiBar?.let { newGeogs[it.first] = it.second; currentMultiBar = null }
      newGeogs.put(bar, geog)
    }
  }
  currentMultiBar?.let { newGeogs[it.first] = it.second; currentMultiBar = null }


  return newGeogs.toSortedMap()
}