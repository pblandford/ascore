package com.philblandford.kscore.engine.core.areadirectory.header

import com.philblandford.kscore.engine.core.HeaderGeography
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.representation.STAVE_HEADER_GAP
import com.philblandford.kscore.engine.time.timeSignature
import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

data class HeaderArea(val base: Area, val startGeog: HeaderGeography? = null) {

  val lookup = mapOf(
    "Clef" to base.tagMap["Clef"]?.firstOrNull(),
    "KeySignature" to base.tagMap["KeySignature"]?.firstOrNull(),
    "TimeSignature" to base.tagMap["TimeSignature"]?.firstOrNull()
  )
  val geog = startGeog ?: createGeog()

  private fun getSectionWidth(key: String): Int {
    return lookup[key]?.let {
      it.second.width + STAVE_HEADER_GAP
    } ?: 0
  }

  private fun createGeog(): HeaderGeography {
    val clefWidth = getSectionWidth("Clef")
    val keyWidth = getSectionWidth("KeySignature")
    val timeWidth = getSectionWidth("TimeSignature")
    return HeaderGeography(keyWidth, timeWidth, clefWidth)
  }
}

private const val cacheId = "HEADER"

fun DrawableFactory.headerArea(hash: Map<EventType, Event>): HeaderArea? {

  return getOrCreate(cacheId, hash) {
    var area = Area(tag = "Header")
    area = addClef(hash, area)
    area = addKey(hash, area)
    area = addTimeSignature(hash, area)
    area = area.createTagMap()
    HeaderArea(area.extendRight(STAVE_HEADER_GAP))
  }
}

private fun DrawableFactory.addClef(hash: Map<EventType, Event>, area: Area): Area {
  return hash[EventType.CLEF]?.let {
    clefArea(it)?.let { area.addRight(it.copy(extra = "header"), gap = STAVE_HEADER_GAP) }
  } ?: area
}

private fun DrawableFactory.addKey(hash: Map<EventType, Event>, area: Area): Area {
  return hash[EventType.CLEF]?.let { clef ->
    hash[EventType.KEY_SIGNATURE]?.let { key ->
      keySignatureArea(key.addParam(EventParam.CLEF to clef.subType as ClefType))?.let {
        area.addRight(it, STAVE_HEADER_GAP)
      }
    }
  } ?: area
}

private fun DrawableFactory.addTimeSignature(hash: Map<EventType, Event>, area: Area): Area {
  return hash[EventType.TIME_SIGNATURE]?.let { ts ->
    timeSignature(ts)?.let {
      val tsArea = timeSignatureArea(it)
      area.addRight(tsArea, STAVE_HEADER_GAP)
    }
  } ?: area
}