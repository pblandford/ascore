package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.SlurArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.TIE_OVERHANG_END
import com.philblandford.kscore.engine.core.representation.TIE_OVERHANG_START
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.duration.realDuration
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.types.*
import kotlin.math.max
import kotlin.math.min

private data class TieDescr(val id: Int, val up: Boolean, val open: Boolean)

object TieDecorator : Decorator {

  override fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area, drawableFactory: DrawableFactory
  ): Area {
    var copy = staveArea
    val grouped = getTieGroups(eventHash, stavePositionFinder)
    grouped.forEach { (address, hash) ->
      drawableFactory.getGroupArea(address, hash, stavePositionFinder)?.let { (area, x) ->
        copy = copy.addArea(area, Coord(x, 0), address)
      }
    }
    return copy
  }

  private fun DrawableFactory.getGroupArea(
    eventAddress: EventAddress,
    eventHash: EventHash, stavePositionFinder: StavePositionFinder
  ): Pair<Area, Int>? {
    return stavePositionFinder.getScoreQuery().getEvent(EventType.DURATION, eventAddress)
      ?.let { chord ->
        stavePositionFinder.getOffsetLookup().addDuration(eventAddress, chord.realDuration())
          ?.let { endAddress ->
            val descriptors = getDescriptors(
              eventHash,
              chord
            ) { stavePositionFinder.getScoreQuery().numVoicesAt(it) }
            val startX = getStartX(eventAddress, stavePositionFinder)
            val endX = getEndX(endAddress, stavePositionFinder)
            var mainArea = Area(tag = "TieGroup")
            eventHash.forEach { (emk, event) ->
              descriptors[emk.eventAddress.id]?.let { descr ->
                mainArea = addTieArea(chord, event, emk.eventAddress, descr, startX, endX, mainArea)
              }
            }
            mainArea to startX
          }
      }
  }

  private fun getStartX(eventAddress: EventAddress, stavePositionFinder: StavePositionFinder): Int {
    return stavePositionFinder.getSlicePosition(eventAddress)?.xMargin
      ?: (stavePositionFinder.getStartBars() -
              TIE_OVERHANG_START)
  }

  private fun getEndX(endAddress: EventAddress, stavePositionFinder: StavePositionFinder): Int {
    return stavePositionFinder.getSlicePosition(endAddress)?.xMargin
      ?: (stavePositionFinder.getEndBars() +
              TIE_OVERHANG_END)
  }

  private fun DrawableFactory.addTieArea(
    chord: Event, tie: Event, eventAddress: EventAddress,
    descr: TieDescr, startX: Int, endX: Int,
    mainArea: Area
  ): Area {
    val yPos = getYPos(chord, descr)
    val startAdjustment = getXAdjustment(descr, true, chord)
    val endAdjustment = getXAdjustment(descr, false, chord)

    return slurArea(
      Coord(startX + startAdjustment, yPos),
      Coord(endX + endAdjustment, yPos),
      descr
    )?.let { (slur, top) ->
      mainArea.addArea(
        slur.copy(event = tie, tag = "Tie", addressRequirement = AddressRequirement.EVENT),
        Coord(startAdjustment, top),
        eventAddress
      )
    } ?: mainArea
  }

  private fun getXAdjustment(tieDescr: TieDescr, start: Boolean, chord:Event): Int {
    val reallyOpen = getReallyOpen(tieDescr, start, chord)

    return if (reallyOpen) {
      if (start) (BLOCK_HEIGHT * 1.5).toInt() else (BLOCK_HEIGHT * 0.75).toInt()
    } else {
      if (start) BLOCK_HEIGHT * 3 else -BLOCK_HEIGHT / 2
    }
  }

  private fun getReallyOpen(tieDescr: TieDescr, start: Boolean, chord:Event):Boolean {
    if (!tieDescr.open) {
      return false
    }

    val upstem = chord.isTrue(EventParam.IS_UPSTEM)

    return if (start) {
      !(tieDescr.up && upstem)
    } else {
      !(!tieDescr.up && !upstem)
    }

  }

  private fun getYPos(chordEvent: Event, tieDescr: TieDescr): Int {
    return chord(chordEvent)?.notes?.toList()?.sortedBy { -it.pitch.midiVal }?.getOrNull(tieDescr.id - 1)?.position?.y?.let { yPos ->
      if (tieDescr.open) {
        if (tieDescr.up) yPos * BLOCK_HEIGHT - BLOCK_HEIGHT * 2 else yPos * BLOCK_HEIGHT + BLOCK_HEIGHT * 2
      } else {
        yPos * BLOCK_HEIGHT
      }
    } ?: 0
  }

  private fun getDescriptors(
    eventHash: EventHash,
    chord: Event,
    getNumVoices: (EventAddress) -> Int
  ): Map<Int, TieDescr> {
    return eventHash.toList().sortedBy { it.first.eventAddress.id }.withIndex().associate { iv ->
      iv.value.first.eventAddress.id to getDescriptor(
        eventHash.size, iv.index, iv.value.first.eventAddress.id, chord,
        getNumVoices(iv.value.first.eventAddress), iv.value.first.eventAddress.voice
      )
    }
  }

  private fun getDescriptor(
    numEvents: Int, eventIdx: Int, eventId: Int, chord: Event,
    numVoices: Int, voice: Int
  ): TieDescr {
    val open = eventId == 1 || eventId == chord.getParam<List<Event>>(EventParam.NOTES)?.size
    val up = when {
      numVoices > 1 -> {
        voice == 1
      }
      numEvents == 1 -> {
        !chord.isTrue(EventParam.IS_UPSTEM) || chord.getParam<List<Event>>(EventParam.NOTES)?.size != 1
      }
      else -> {
        eventIdx < numEvents / 2
      }
    }
    return TieDescr(eventId, up, open)
  }

  private fun getTieGroups(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder
  ): Map<EventAddress, EventHash> {
    val filtered = eventHash.mapNotNull { (key, event) ->
      getEvent(key.eventAddress, event, stavePositionFinder)
    }
    return filtered.toList().groupBy { it.first.eventAddress.idless() }
      .map { it.key to it.value.toMap() }.toMap()
  }

  private fun getEvent(
    eventAddress: EventAddress, event: Event,
    stavePositionFinder: StavePositionFinder
  ): Pair<EventMapKey, Event>? {
    val offsetLookup = stavePositionFinder.getOffsetLookup()
    val inRange = offsetLookup.addDuration(eventAddress, event.duration())?.let { end ->
      eventAddress.barNum <= stavePositionFinder.getEndBar() &&
          end.barNum >= stavePositionFinder.getStartBar()
    } ?: false
    val isEnd = event.isTrue(EventParam.IS_END_TIE)
    return  if (!isEnd && inRange) {
      EMK(EventType.TIE, eventAddress) to event
    } else {
      null
    }
  }

  private fun DrawableFactory.slurArea(startCoord: Coord, endCoord: Coord, tieDescr: TieDescr): Pair<Area, Int>? {
    return if (tieDescr.up) {
      slurAreaUp(startCoord, endCoord, tieDescr.open)
    } else {
      slurAreaDown(startCoord, endCoord, tieDescr.open)
    }
  }

  private fun bulge(open:Boolean, width:Int):Int {
    return if (open) width/10 else width /16
  }

  private fun DrawableFactory.slurAreaUp(startCoord: Coord, endCoord: Coord, open: Boolean): Pair<Area, Int>? {
    val width = endCoord.x - startCoord.x
    val top = min(startCoord.y, endCoord.y) - bulge(open, width)
    return getDrawableArea(
      SlurArgs(
        Coord(0, startCoord.y - top),
        Coord((endCoord.x - startCoord.x) / 2, 0),
        Coord(endCoord.x - startCoord.x, endCoord.y - top),
        true
      )
    )?.let { it to top }
  }

  private fun DrawableFactory.slurAreaDown(startCoord: Coord, endCoord: Coord, open: Boolean): Pair<Area, Int>? {
    val width = endCoord.x - startCoord.x
    val bottom = max(startCoord.y, endCoord.y) + bulge(open, width)
    val top = min(startCoord.y, endCoord.y)
    return getDrawableArea(
      SlurArgs(
        Coord(0, 0),
        Coord((endCoord.x - startCoord.x) / 2, bottom - top),
        Coord(endCoord.x - startCoord.x, 0),
        false
      )
    )?.let { it to top }
  }

}