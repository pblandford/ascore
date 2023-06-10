package com.philblandford.kscore.engine.core.stave

import BeamDescriptor
import com.philblandford.kscore.engine.beam.Beam
import com.philblandford.kscore.engine.beam.BeamMap
import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.core.StemGeography
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.BeamArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.addC
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.minus
import com.philblandford.kscore.engine.duration.plus
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.Lookup
import com.philblandford.kscore.engine.types.OffsetLookup
import com.philblandford.kscore.engine.types.StavePositionFinder
import com.philblandford.kscore.engine.types.lookupOf
import com.philblandford.kscore.log.ksLoge
import com.philblandford.kscore.util.highestBit
import getBeamDescriptors
import kotlin.math.abs
import kotlin.math.min

private data class BeamMember(val slicePosition: SlicePosition, val stemGeography: StemGeography)
private data class BeamDimensions(
  val start: Coord, val end: Coord, val gradient: Float,
  val numBeams: Int
) {
  val up = end.y < start.y
  val width = end.x - start.x
  val height = abs(end.y - start.y)
  val beamHeight = numBeams * (BEAM_THICKNESS + BEAM_GAP) - BEAM_GAP
  val top = min(end.y, start.y)
}

/*
 * Draw the beams onto a stave area - it adds them as areas directly onto the stave area.
 * Often the lengths of the beamed chords will need to change, so it returns a lookup of the
 * altered stem geographies so the caller can replace them
 */
fun DrawableFactory.drawBeams(
  beams: BeamMap, stavePositionFinder: StavePositionFinder,
  staveArea: Area
): Pair<Area, Lookup<StemGeography>> {
  var areaCopy = staveArea
  val offsetLookup = stavePositionFinder.getScoreQuery()
  val startOffset = offsetLookup.addressToOffset(stavePositionFinder.getFirstSegment()) ?: dZero()
  val endOffset = offsetLookup.addressToOffset(stavePositionFinder.getLastSegment()) ?: dZero()
  var stemLookup = lookupOf<StemGeography>()

  beams.forEach { (eventAddress, beam) ->
    val (adjustedAddress, adjustedBeam) =
      if (eventAddress.barNum < stavePositionFinder.getStartBar()) {
        val beamOffset = offsetLookup.addressToOffset(eventAddress)!!
        val offsetAdjustment = startOffset - beamOffset
        val members = beam.members.dropWhile {
        beamOffset + it.offset < startOffset
        }.map { it.copy(offset = it.offset - offsetAdjustment ) }
        stavePositionFinder.getFirstSegment().copy(voice = eventAddress.voice) to Beam(members, up = beam.up)
      } else if (offsetLookup.addressToOffset(eventAddress)!! + beam.members.last().offset <= endOffset) {
        eventAddress to beam
      }
      else {
        val (address, members) = eventAddress to beam.members.takeWhile { offsetLookup.addressToOffset(eventAddress)!! + it.offset <= endOffset }
        address to Beam(members, up = beam.up)
      }

    val members = adjustedBeam.members.mapNotNull { bm ->
      val address =
        if (adjustedAddress.isGrace) adjustedAddress.copy(graceOffset = bm.offset) else stavePositionFinder.getScoreQuery()
          .addDuration(adjustedAddress, bm.offset) ?: eventAddress

      stavePositionFinder.getSlicePosition(address.voiceless())?.let { sp ->
        stavePositionFinder.getStemGeography(address)?.let { sg ->
          bm.offset to BeamMember(sp, sg)
        }
      }
    }.toMap()
    val (area, stemLookupFromBeamArea) = addBeamArea(
      adjustedBeam, members, adjustedAddress, areaCopy,
      stavePositionFinder.getScoreQuery()
    )
    areaCopy = area
    stemLookup = stemLookup.plus(stemLookupFromBeamArea)
  }
  return Pair(areaCopy, stemLookup)
}

private fun DrawableFactory.addBeamArea(
  beam: Beam, members: Map<Offset, BeamMember>,
  eventAddress: EventAddress,
  main: Area, offsetLookup: OffsetLookup
): Pair<Area, Lookup<StemGeography>> {
  if (members.isEmpty()) {
    return Pair(main, lookupOf())
  }
  return run {//getOrCreate("BEAM_AREA", Triple(beam, members, eventAddress)) {
    val list = members.toList().sortedBy { it.first }
    val first = list.first().second
    val last = list.last().second

    var newArea = main

    val beamDescriptors = getBeamDescriptors(beam)
    val beamDepth = beamDescriptors.groupBy { it.start }.maxBy { it.value.size }.value.size
    val beamDimensions = getBeamDimensions(first, last, beamDepth)

    val extra =
      (beamDescriptors.map { it.duration }.distinct().size - 2) * BEAM_GAP + BEAM_THICKNESS
    val adjustRes = getStemGeographies(members, beamDimensions, beam.up, eventAddress.isGrace)
    val stemGeogsAdjust = adjustRes.first
    val stemAdjustment = abs(adjustRes.second)

    beamDescriptors.forEach { descriptor ->
      val membersForDescriptor = members.filter { it.key in descriptor.members }
      if (membersForDescriptor.isNotEmpty()) {
        newArea = getBeamDrawable(
          descriptor, beam, beamDimensions, extra + stemAdjustment, eventAddress, stemGeogsAdjust,
          membersForDescriptor, newArea, eventAddress
        )
      }
    }
    val lookup = addStemExtra(stemGeogsAdjust, extra, eventAddress, offsetLookup)
    Pair(newArea, lookup)
  }
}

private fun addStemExtra(
  stems: Map<Offset, StemGeography>,
  extra: Int, eventAddress: EventAddress,
  offsetLookup: OffsetLookup
): Lookup<StemGeography> {
  return stems.map { (offset, stem) ->
    val address = if (eventAddress.isGrace) {
      eventAddress.copy(graceOffset = offset)
    } else {
      offsetLookup.addDuration(eventAddress, offset) ?: eventAddress
    }
    address to
        if (stem.up) {
          stem.copy(tip = stem.tip - extra)
        } else {
          stem.copy(tip = stem.tip + extra)
        }
  }.toMap()
}

private fun DrawableFactory.getBeamDrawable(
  descriptor: BeamDescriptor, beam: Beam, beamDimensions: BeamDimensions,
  extra: Int, beamAddress: EventAddress,
  stemGeogs: Map<Offset, StemGeography>,
  members: Map<Offset, BeamMember>,
  mainArea: Area, eventAddress: EventAddress
): Area {
  var newArea = mainArea
  val firstOffset = members.toList().first().first
  val beamNum = descriptor.duration.denominator.highestBit() - 4
  val beamOffset = beamAddress.graceOffset ?: beamAddress.offset
  val offsetYWithinBeam = if (beam.up) beamNum * (BEAM_GAP + BEAM_THICKNESS)
  else -beamNum * (BEAM_GAP + BEAM_THICKNESS) - BEAM_THICKNESS
  val offsetY = if (beam.up) offsetYWithinBeam - extra
  else offsetYWithinBeam + extra
  members[descriptor.start]?.let { startMember ->
    members[descriptor.end]?.let { endMember ->
      stemGeogs[descriptor.end]?.let { endGeog ->

        val dimensions = getSingleBeamDimensions(
          descriptor, beamOffset, beamDimensions, startMember,
          endMember, beamOffset + firstOffset, offsetY
        )

        getDrawableArea(
          BeamArgs(
            beamDimensions.up,
            dimensions.width,
            dimensions.height
          )
        )?.let { singleBeam ->
          newArea = newArea.addArea(
            singleBeam.copy(tag = "Beam"), Coord(dimensions.start.x, dimensions.top),
            eventAddress
          )
        }
      }
    }
  }
  return newArea
}

private fun getBeamDimensions(first: BeamMember, last: BeamMember, numBars: Int): BeamDimensions {
  val startBeamX = first.slicePosition.xMargin + first.stemGeography.xPos
  val endBeamX = last.slicePosition.xMargin + last.stemGeography.xPos
  val startBeamY = first.stemGeography.tip
  val endBeamY = last.stemGeography.tip
  val beamWidth = endBeamX - startBeamX

  val gradient = (endBeamY - startBeamY).toFloat() / beamWidth
  return if (abs(gradient) > BEAM_MAX_GRADIENT) {
    val up = startBeamY > endBeamY
    val height = (BEAM_MAX_GRADIENT * beamWidth).toInt()
    if (up) {
      BeamDimensions(
        Coord(startBeamX, endBeamY + height),
        Coord(endBeamX, endBeamY),
        -BEAM_MAX_GRADIENT,
        numBars
      )
    } else {
      BeamDimensions(
        Coord(startBeamX, startBeamY),
        Coord(endBeamX, startBeamY + height),
        BEAM_MAX_GRADIENT,
        numBars
      )
    }
  } else {
    Pair(gradient, Pair(startBeamY, endBeamY))
    BeamDimensions(Coord(startBeamX, startBeamY), Coord(endBeamX, endBeamY), gradient, numBars)
  }
}

private fun getSingleBeamDimensions(
  descriptor: BeamDescriptor, beamOffset: Offset,
  beamDimensions: BeamDimensions, startMember: BeamMember,
  endMember: BeamMember, firstOffset: Offset, offsetY: Int
): BeamDimensions {
  val miniBeam = descriptor.start == descriptor.end
  val startX = if (miniBeam &&  firstOffset != beamOffset) {
    startMember.slicePosition.xMargin + startMember.stemGeography.xPos - MINIBEAM_WIDTH
  } else {
    startMember.slicePosition.xMargin + startMember.stemGeography.xPos
  }
  val endX = if (miniBeam && firstOffset == beamOffset) {
    startMember.slicePosition.xMargin + startMember.stemGeography.xPos + MINIBEAM_WIDTH
  } else {
    endMember.slicePosition.xMargin + endMember.stemGeography.xPos
  }

  val startY =
    beamDimensions.start.y + ((startX - beamDimensions.start.x) * beamDimensions.gradient).toInt() + offsetY

  val endY =
    beamDimensions.start.y + ((endX - beamDimensions.start.x) * beamDimensions.gradient).toInt() + offsetY

  return BeamDimensions(
    Coord(startX, startY),
    Coord(endX, endY),
    beamDimensions.gradient,
    beamDimensions.numBeams
  )
}

private fun getStemGeographies(
  members: Map<Offset, BeamMember>,
  beamDimensions: BeamDimensions,
  up: Boolean,
  grace:Boolean,
  adjustSoFar: Int = 0,
  numCalls: Int = 0
): Pair<Map<Offset, StemGeography>, Int> {
  val clearance = if (grace) STEM_MIN_CLEAR_GRACE else STEM_MIN_CLEAR

  val geogs = members.map { (offset, member) ->
    val absoluteXPos = member.slicePosition.xMargin + member.stemGeography.xPos
    val tip =
      beamDimensions.start.y + ((absoluteXPos - beamDimensions.start.x) * beamDimensions.gradient).toInt()
    offset to StemGeography(
      tip, member.stemGeography.base, member.stemGeography.noteArea,
      member.stemGeography.xPos, up, beamDimensions.beamHeight
    )
  }.toMap()
  val shortest =
    (geogs.minByOrNull { it.value.exposed }?.value?.exposed ?: 0) - beamDimensions.beamHeight
  return if (shortest < clearance) {
    val adjustment =
      (if (!up) clearance - shortest else shortest - clearance)
    getStemGeographies(
      members,
      beamDimensions.copy(
        start = Coord(beamDimensions.start.x, beamDimensions.start.y + adjustment),
        end = Coord(beamDimensions.end.x, beamDimensions.end.y + adjustment)
      ),
      up,
      grace,
      adjustment + adjustSoFar,
      numCalls + 1
    )
  } else {
    geogs to adjustSoFar
  }
}

