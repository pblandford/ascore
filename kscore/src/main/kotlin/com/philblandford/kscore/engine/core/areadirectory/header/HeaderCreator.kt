package com.philblandford.kscore.engine.core.areadirectory.header

import com.philblandford.kscore.engine.core.HeaderGeography
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.AreaDirectory
import com.philblandford.kscore.engine.types.BarNum
import com.philblandford.kscore.engine.types.Lookup
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.kscore.engine.types.ea

/* Creates the header areas - the areas at the start of each line of music displaying clef,
  key signature and time signature */


/* Returns two lookups - one for the areas themselves, one for just their geographies */
fun DrawableFactory.createHeaders(
  scoreQuery: ScoreQuery, existing: AreaDirectory?,
  createHeaders: Boolean
): Pair<Lookup<HeaderGeography>, Lookup<HeaderArea>>? {

  if (!createHeaders) {
    existing?.let { return Pair(it.headerGeogLookup, it.headerLookup) }
  }

  /* First find out what events are needed for each header */
  val headerEventMaps = createHeaderEventMaps(scoreQuery)

  val areas = createAreas(scoreQuery, headerEventMaps)
  val alignedAreas = alignHeaders(areas)
  val geogs = alignedAreas.map {
    it.key to it.value.geog
  }.toMap()
  return Pair(geogs, alignedAreas)
}

/* Create the raw areas, unaligned */
private fun DrawableFactory.createAreas(
  scoreQuery: ScoreQuery,
  headerEventMaps: Lookup<HeaderEventMap>
): Lookup<HeaderArea> {
  return scoreQuery.getAllStaves(true).flatMap { stave ->
    (1..scoreQuery.numBars).mapNotNull { bar ->
      val addr = ea(bar).copy(staveId = stave)
      val hash = headerEventMaps[addr] ?: mapOf()
      headerArea(hash)?.let { addr to it }
    }
  }.toMap()
}

/* Align all events in the headers */
private fun DrawableFactory.alignHeaders(headers: Lookup<HeaderArea>): Lookup<HeaderArea> {
  val alignedGeogs = createAlignedGeogs(headers)
  return headers.mapNotNull { (address, header) ->
    alignedGeogs[address.barNum]?.let { geog ->
      val newHeader = alignHeader(geog, header)
      address to newHeader.copy(base = newHeader.base.transformEventAddress { _, _ -> address })
    }
  }.toMap()
}

/* Create a new header area based on a master geography */
private fun DrawableFactory.alignHeader(masterGeography: HeaderGeography, headerArea: HeaderArea): HeaderArea {

  return getOrCreate(ALIGNED_HEADER_CACHE, masterGeography to headerArea) {
    var base = Area(tag = "Header")
    headerArea.lookup["Clef"]?.let { clef ->
      base = base.addArea(clef.second, Coord(masterGeography.clefStart))
    }
    headerArea.lookup["KeySignature"]?.let { key ->
      base = base.addArea(key.second, Coord(masterGeography.keyStart))
    }
    headerArea.lookup["TimeSignature"]?.let { time ->
      base = base.addArea(time.second, Coord(masterGeography.timeStart))
    }
    HeaderArea(base, masterGeography)
  }
}

private val ALIGNED_HEADER_CACHE = "AlignedHeader"

/* Create a master geography for all headers, so all events of the same type line up */
private fun createAlignedGeogs(headers: Lookup<HeaderArea>): Map<BarNum, HeaderGeography> {
  val grouped = headers.toList().groupBy { it.first.barNum }

  return grouped.map { (barNum, headers) ->
    barNum to createAlignedGeog(headers.map { it.second })
  }.toMap()
}

/* Create an alignment geography based on the widest area of each type of all header areas */
private fun createAlignedGeog(headers: Iterable<HeaderArea>): HeaderGeography {
  val clefWidth = headers.maxByOrNull { it.geog.clefWidth }?.geog?.clefWidth ?: 0
  val keyWidth = headers.maxByOrNull { it.geog.keyWidth }?.geog?.keyWidth ?: 0
  val timeWidth = headers.maxByOrNull { it.geog.timeWidth }?.geog?.timeWidth ?: 0
  return HeaderGeography(keyWidth, timeWidth, clefWidth)
}