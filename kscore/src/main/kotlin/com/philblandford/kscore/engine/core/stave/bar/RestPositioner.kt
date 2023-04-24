package com.philblandford.kscore.engine.core.stave.bar

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.areadirectory.segment.SegmentArea
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.REST_LINE_V1
import com.philblandford.kscore.engine.core.representation.REST_LINE_V2
import com.philblandford.kscore.engine.core.representation.REST_MAX_HEIGHT
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.types.DurationType
import kotlin.math.max
import kotlin.math.min


internal fun getRestShifts(segmentAreas: Map<Duration, SegmentArea>): Map<Int, Int> {
  val map = mutableMapOf(1 to 0, 2 to 0)

  segmentAreas.forEach { (_, sa) ->
    val local = getRestPositionLocal(sa)
    local.forEach { (k, v) ->
      map[k]?.let { current ->
        val new = if (k == 1) {
          min(v, current ?: 0)
        } else {
          max(v, current ?: 0)
        }
        map.put(k, new)
      }
    }
  }
  return map
}

private fun getRestPositionLocal(segmentArea: SegmentArea): Map<Int, Int> {
  if (segmentArea.voiceGeographies.size < 2) {
    return mapOf(1 to 0, 2 to 0)
  } else {
    return (1..2).mapNotNull { voice ->
      getRestPosition(segmentArea, voice)
        ?.let { Pair(voice, it) }
    }.toMap()
  }
}

private fun getRestPosition(segmentArea: SegmentArea, voice: Int): Int? {
  val thisGeog = segmentArea.voiceGeographies[voice]
  val otherGeog =
    if (voice == 1) segmentArea.voiceGeographies[2] else segmentArea.voiceGeographies[1]

  return thisGeog?.let {
    otherGeog?.let {
      if (thisGeog.notePositions.count() == 0) {
        if (voice == 1) {
          if (otherGeog.isRest) {
            REST_LINE_V1
          } else {
            min(otherGeog.top - REST_MAX_HEIGHT - BLOCK_HEIGHT, REST_LINE_V1)
          }
        } else {
          if (otherGeog.isRest) {
            REST_LINE_V2
          } else {
            max(otherGeog.bottom + BLOCK_HEIGHT, REST_LINE_V2)
          }
        }
      } else {
        null
      }
    }
  }
}

private val unchangedMap = mapOf(1 to 0, 2 to 0)
internal fun adjustRests(segmentArea: SegmentArea, restShifts: Map<Int, Int>): SegmentArea {
  val unchanged = unchangedMap
  if (restShifts != unchanged) {
    val newChildMap = segmentArea.base.childMap.toMutableList()

    segmentArea.base.childMap.forEach { (k, v) ->
      if (v.event?.subType == DurationType.REST) {
        restShifts[k.eventAddress.voice]?.let { shift ->
          newChildMap.remove(k to v)
          newChildMap.add(k.copy(coord = Coord(k.coord.x, shift)) to v)
        }
      }
    }
    val voiceGeographies = segmentArea.voiceGeographies.map { (voice, voiceGeog) ->
      voiceGeog.restPos?.let {
        restShifts[voice]?.let { shift ->
          voice to voiceGeog.copy(restPos = voiceGeog.restPos + shift)
        }
      } ?: (voice to voiceGeog)
    }.toMap()
    val newBase = segmentArea.base.copy(childMap = newChildMap)
    return SegmentArea(
      newBase,
      segmentArea.duration,
      voiceGeographies,
      segmentArea.voicePositions,
      segmentArea.graceSlicePositions
    )
  } else {
    return segmentArea
  }
}