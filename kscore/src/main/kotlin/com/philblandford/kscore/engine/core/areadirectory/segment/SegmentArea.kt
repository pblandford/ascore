package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.SegmentGeography
import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.core.VoiceGeography
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.AreaMapKey
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.areadirectory.header.clefArea
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.ORNAMENT_OFFSET
import com.philblandford.kscore.engine.core.representation.STAVE_HEADER_GAP
import com.philblandford.kscore.engine.core.representation.STAVE_HEIGHT
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.types.*

/* A segment on the stave - contains areas for individual voices, plus any accidentals */

data class SegmentArea(
  val base: Area,
  val duration: Duration,
  val voiceGeographies: Map<Int, VoiceGeography>,
  val voicePositions: Map<Int, Int>,
  val graceSlicePositions: Map<Offset, SlicePosition>,
  val placeHolder: Boolean = false
) {
  val geography =
    SegmentGeography(
      base.width, base.xMargin, duration, voiceGeographies, voicePositions,
      graceSlicePositions, placeHolder
    )
  val hash = hashCode()
}

internal fun DrawableFactory.segmentArea(
  allEvents: EventHash,
  eventAddress: EventAddress,
  numVoices: Int = 1,
  graceSegments: Map<Offset, SegmentArea> = mapOf()
): SegmentArea? {

  val events = allEvents.filter { it.key.eventAddress.isGrace == eventAddress.isGrace }
  val duration = events.maxByOrNull { it.value.realDuration() }?.value?.realDuration() ?: dZero()

  var base = Area(tag = "Segment", height = STAVE_HEIGHT)

  val voiceAreas = createVoiceAreas(events, eventAddress, numVoices)
  val res = addVoiceAreas(voiceAreas, events, base)
  base = res.first
  val positions = res.second
  base = createAccidentalArea(events, base)
  base = createArpeggioArea(events, base)
  base = createClefArea(events, base, eventAddress)
  val pair = createGraceArea(graceSegments, base)
  base = pair.first
  val graceSlices = pair.second
  val graceSlicesRelToXMargin = graceSlices.map { (k, v) ->
    k to SlicePosition(base.xMargin - v.start, base.xMargin - v.xMargin, v.width)
  }.toMap()

  base = base.transformEventAddress { ea, _ ->
    eventAddress.copy(
      graceOffset = eventAddress.graceOffset ?: ea.graceOffset,
      voice = ea.voice,
      id = ea.id
    )
  }

  val voiceGeographies =
    voiceAreas.map { it.key.eventAddress.voice to it.value.voiceGeography }.toMap()

  val placeHolder =
    allEvents.size == 1 && allEvents.toList().first().first.eventType == EventType.PLACE_HOLDER

  return SegmentArea(
    base,
    duration,
    voiceGeographies,
    positions,
    graceSlicesRelToXMargin,
    placeHolder
  )
}

/* Replace a single voice area within a segment (so far this only happens when a chord has
 * been beamed and the length of its stem might have changed)
 */
internal fun SegmentArea.replaceVoiceArea(
  voice: Int,
  transform: (AreaMapKey, VoiceArea) -> VoiceArea
): SegmentArea {
  return base.childMap.toList().find {
    it.second.tag == "Chord" &&
        it.first.eventAddress.voice == voice
  }?.let { (voiceKey, voiceArea) ->
    voiceGeographies[voice]?.let { voiceGeog ->
      var newVoiceArea = transform(voiceKey, VoiceArea(voiceArea, voiceGeog))
      newVoiceArea =
        newVoiceArea.copy(base = newVoiceArea.base.transformEventAddress { ea, _ ->
          voiceKey.eventAddress.copy(
            id = ea.id
          )
        })
      val newBase = base.copy(childMap = base.childMap.minus(voiceKey)).addArea(
        newVoiceArea.base,
        voiceKey.coord, voiceKey.eventAddress.copy()
      )
      return copy(
        base = newBase,
        voiceGeographies = voiceGeographies.plus(voice to newVoiceArea.voiceGeography)
      )
    }
  } ?: this
}

/* Remove a grace area and replace it with a new one - used when grace notes have been beamed
 * and their stems are now different lengths
 */
internal fun SegmentArea.replaceGraceArea(
  eventAddress: EventAddress,
  graceSegments: Lookup<SegmentArea>
): SegmentArea {
  return base.childMap.toList().find { it.second.tag == "Grace" }?.let { (ogKey, oldGrace) ->
    val newGraceChildMap =
      oldGrace.childMap.toList().fold(oldGrace.childMap) { childMap, (oldKey, oldSegment) ->
        graceSegments[oldKey.eventAddress]?.let { gs ->
          childMap.plus(oldKey to gs.base.copy(width = oldSegment.width,
          addressRequirement = oldSegment.addressRequirement, tag = oldSegment.tag))
        } ?: childMap
      }
    val graceArea = oldGrace.copy(childMap = newGraceChildMap)
    val newMap = base.childMap.plus(ogKey to graceArea)

    var newBase = base.copy(childMap = newMap)
    newBase =
      newBase.transformEventAddress { ea, _ ->
        eventAddress.copy(
          graceOffset = ea.graceOffset,
          voice = ea.voice,
          id = ea.id
        )
      }

     copy(base = newBase)
  } ?: this
}

private fun DrawableFactory.createVoiceAreas(
  events: EventHash,
  eventAddress: EventAddress,
  numVoices: Int
): Map<AreaMapKey, VoiceArea> {
  return events.mapNotNull { (key, event) ->
    voiceArea(event, key.eventAddress.voice, numVoices)?.let {
      val transformed = VoiceArea(
        it.base.transformEventAddress { ea, _ -> ea.copy(voice = key.eventAddress.voice) },
        it.voiceGeography
      )
      AreaMapKey(Coord(0, 0), eventAddress.copy(voice = key.eventAddress.voice)) to transformed
    }
  }.toMap()
}

private fun addVoiceAreas(
  voiceAreas: Map<AreaMapKey, VoiceArea>,
  events: EventHash,
  base: Area
): Pair<Area, Map<Int, Int>> {
  val inputs = events.map { it.key.eventAddress.voice to it.value }.toMap()
  val positions = positionAreas(inputs)
  var baseCopy = base
  voiceAreas.forEach {
    val xPos = positions[it.key.eventAddress.voice] ?: 0
    baseCopy = baseCopy.addArea(
      it.value.base,
      it.key.coord.plusX(xPos),
      it.key.eventAddress
    )
  }
  return Pair(baseCopy, positions)
}

private fun createGraceArea(
  segments: Map<Offset, SegmentArea>,
  base: Area
): Pair<Area, Map<Offset, SlicePosition>> {
  val graceArea = graceArea(segments)
  val slicePositions =
    graceArea.childMap.filter { it.value.tag == "Segment" }
      .map { (key, area) ->
        (key.eventAddress.graceOffset ?: dZero()) to SlicePosition(
          key.coord.x - area.xMargin,
          key.coord.x,
          area.width
        )
      }.toMap()
  val area = base.addLeft(graceArea)
  return Pair(area, slicePositions)
}

private fun DrawableFactory.createClefArea(
  events: EventHash,
  base: Area,
  eventAddress: EventAddress
): Area {
  return if (eventAddress.offset != dZero()) {
    events.toList().find { it.first.eventType == EventType.CLEF }?.let { (_, clef) ->
      clefArea(clef)?.let {
        base.addLeft(it, gap = STAVE_HEADER_GAP * 2, eventAddress = eventAddress)
      }
    } ?: base
  } else {
    base
  }
}

private fun DrawableFactory.createAccidentalArea(events: EventHash, base: Area): Area {
  val chords = events.filter { it.value.subType == DurationType.CHORD }
  val notes = chords.flatMap { it.value.getParam<Iterable<Event>>(EventParam.NOTES) ?: listOf() }

  val inputs =
    notes.filter { it.getParam<Pitch>(EventParam.PITCH)?.showAccidental ?: false }.map {
      AccidentalInput(
        it.getParam<Coord>(EventParam.POSITION)?.y ?: 0,
        it.getParam<Pitch>(EventParam.PITCH)?.accidental ?: Accidental.SHARP
      )
    }
  val accArea = accidentalArea(inputs)
  return base.addLeft(accArea)
}

private fun DrawableFactory.createArpeggioArea(events: EventHash, base: Area): Area {
  var copy = base
  events.filter { it.value.subType == DurationType.CHORD }.forEach { (key, chordEvent) ->
    chord(chordEvent)?.let { chord ->
      chord.arpeggio?.let { _ ->
        val notes = chord.notes.toList().sortedBy { it.position.y }
        val height = ((notes.last().position.y - notes.first().position.y + 1) + 2) * BLOCK_HEIGHT
        arpeggioArea(height)?.let { arpArea ->
          copy = copy.addLeft(
            arpArea, ORNAMENT_OFFSET, notes.first().position.y * BLOCK_HEIGHT - BLOCK_HEIGHT,
            key.eventAddress
          )
        }
      }
    }
  }
  return copy
}

