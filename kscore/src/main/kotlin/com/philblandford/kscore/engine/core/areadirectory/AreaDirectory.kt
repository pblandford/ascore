package com.philblandford.kscore.engine.core.areadirectory

import com.philblandford.kscore.engine.core.BarEndGeographyPair
import com.philblandford.kscore.engine.core.BarStartGeographyPair
import com.philblandford.kscore.engine.core.HeaderGeography
import com.philblandford.kscore.engine.core.PreHeaderGeography
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarEndAreaPair
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarStartAreaPair
import com.philblandford.kscore.engine.core.areadirectory.barstartend.createBarEnds
import com.philblandford.kscore.engine.core.areadirectory.barstartend.createBarStarts
import com.philblandford.kscore.engine.core.areadirectory.header.HeaderArea
import com.philblandford.kscore.engine.core.areadirectory.header.createHeaders
import com.philblandford.kscore.engine.core.areadirectory.preheader.PreHeaderArea
import com.philblandford.kscore.engine.core.areadirectory.preheader.createPreHeaders
import com.philblandford.kscore.engine.core.areadirectory.segment.createSegments
import com.philblandford.kscore.engine.core.areadirectory.text.createFermataWidths
import com.philblandford.kscore.engine.core.areadirectory.text.createHarmonyWidths
import com.philblandford.kscore.engine.core.areadirectory.text.createLyricWidths
import com.philblandford.kscore.engine.core.areadirectory.text.createSegmentExtensions
import com.philblandford.kscore.engine.types.*
import java.util.*

data class AreaDirectory(
  val segmentLookup: SegmentLookup,
  val segmentGeogBarLookup: SegmentGeogBarLookup,
  val segmentStaveLookup: SegmentStaveLookup,
  val preHeaderGeogLookup: Lookup<PreHeaderGeography>,
  val preHeaderLookup: Lookup<PreHeaderArea>,
  val headerGeogLookup: Lookup<HeaderGeography>,
  val headerLookup: Lookup<HeaderArea>,
  val barStartLookup: Lookup<BarStartAreaPair>,
  val barEndLookup: Lookup<BarEndAreaPair>,
  val barStartGeogLookup: Lookup<BarStartGeographyPair>,
  val barEndGeogLookup: Lookup<BarEndGeographyPair>,
  val lyricWidthLookup: Lookup<Int>,
  val harmonyWidthLookup: Lookup<Int>,
  val fermatWidthLookup: Lookup<Int>,
  val segmentExtensionLookup: Lookup<Int>
) : AreaDirectoryQuery {

  override fun getSegmentGeogsForColumn(barNum: Int): SegmentGeogStaveMap? {
    return segmentGeogBarLookup.get(barNum)?.toList()?.groupBy { it.first.staveId }?.map {
      it.key to hashMapOf(*it.value.toTypedArray())
    }?.toMap()
  }

  private val allSGsByBar = doGetAllSegmentGeogsByBar()
  override fun getAllSegmentGeogsByBar(): SortedMap<Int, SegmentGeogStaveMap> {
    return allSGsByBar
  }

  private fun doGetAllSegmentGeogsByBar(): SortedMap<Int, SegmentGeogStaveMap> {
    val list =
      segmentGeogBarLookup.map { it.key to it.value.toList().groupBy { it.first.staveId } }.map {
        it.first to it.second.map {
          it.key to hashMapOf(*it.value.toTypedArray())
        }.toMap()
      }
    return sortedMapOf(*list.toTypedArray())
  }

  override fun getSegmentsForStave(staveId: StaveId): SegmentLookup {
    return segmentStaveLookup[staveId] ?: mapOf()
  }

  override fun getAllPreHeaderGeogs(): Lookup<PreHeaderGeography> {
    return preHeaderGeogLookup
  }

  override fun getAllPreHeaderAreas(): Lookup<PreHeaderArea> {
    return preHeaderLookup
  }

  override fun getAllHeaderGeogs(): Lookup<HeaderGeography> {
    return headerGeogLookup
  }

  override fun getAllHeaderAreas(): Lookup<HeaderArea> {
    return headerLookup
  }

  override fun getAllBarStartAreas(): Lookup<BarStartAreaPair> {
    return barStartLookup
  }

  override fun getAllBarEndAreas(): Lookup<BarEndAreaPair> {
    return barEndLookup
  }

  override fun getAllBarStartGeogs(): Lookup<BarStartGeographyPair> {
    return barStartGeogLookup
  }

  override fun getAllBarEndGeogs(): Lookup<BarEndGeographyPair> {
    return barEndGeogLookup
  }

  override fun getLyricWidths(): Lookup<Int> {
    return lyricWidthLookup
  }

  override fun getHarmonyWidths(): Lookup<Int> {
    return harmonyWidthLookup
  }

  override fun getFermataWidths(): Lookup<Int> {
    return fermatWidthLookup
  }

  override fun getSegmentExtensions(): Lookup<Int> {
    return segmentExtensionLookup
  }
}

fun DrawableFactory.areaDirectory(
  scoreQuery: ScoreQuery,
  existing: AreaDirectory? = null,
  changedBars:List<EventAddress>? = null,
  updateHeaders:Boolean = true
): AreaDirectory? {
  if (changedBars?.isEmpty() == true && !updateHeaders) {
    return existing
  }

  val segmentCreatorReturn = createSegments(scoreQuery, existing, changedBars)
  val headerLookupPair = createHeaders(scoreQuery, existing, updateHeaders)
  val preHeaderLookupPair = createPreHeaders(scoreQuery)
  val barStartLookupPair = createBarStarts(scoreQuery, existing, updateHeaders)
  val barEndLookupPair = createBarEnds(scoreQuery, existing, updateHeaders)
  val lyricWidths = createLyricWidths(scoreQuery)
  val harmonyWidths = createHarmonyWidths(scoreQuery)
  val fermataWidths = createFermataWidths(scoreQuery)
  val segmentExtensions = createSegmentExtensions(scoreQuery)
  return segmentCreatorReturn?.let { segReturn ->
    preHeaderLookupPair?.let { preHeaderPair ->
      headerLookupPair?.let { headerPair ->
        AreaDirectory(
          segReturn.segmentLookup,
          segReturn.segmentGeogBarLookup,
          segReturn.segmentStaveLookup,
          preHeaderPair.first,
          preHeaderPair.second,
          headerPair.first,
          headerPair.second,
          barStartLookupPair.first,
          barEndLookupPair.first,
          barStartLookupPair.second,
          barEndLookupPair.second,
          lyricWidths,
          harmonyWidths,
          fermataWidths,
          segmentExtensions
        )
      }
    }
  }
}

