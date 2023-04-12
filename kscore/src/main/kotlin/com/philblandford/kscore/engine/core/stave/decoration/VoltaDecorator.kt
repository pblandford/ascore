package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.cZero
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.core.areadirectory.header.numberArea
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.VOLTA_HEIGHT
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.StavePositionFinder

object VoltaDecorator : UpDownDecorator {

  override fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area, drawableFactory: DrawableFactory
  ): Area {
    var areaCopy = staveArea

    val groups = groupEvents(eventHash, stavePositionFinder.getStartBar())

    groups.forEach { group ->
      val entries = group.toList().sortedBy { it.first }
      val mainAreaPosPair = drawableFactory.getGroupArea(entries, stavePositionFinder)
      val startX = mainAreaPosPair.second
      val area = mainAreaPosPair.first
      val yPos = staveArea.getTopForRange(startX, startX + area.width) - VOLTA_HEIGHT - area.height
      val shift = entries.first().second.getParam<Coord>(EventParam.HARD_START) ?: cZero()
      areaCopy = areaCopy.addArea(
        area, Coord(startX, yPos) + shift,
        group.entries.first().key.eventAddress
      )
    }
    return areaCopy
  }

  private fun DrawableFactory.getGroupArea(
    entries: List<Pair<EventMapKey, Event>>,
    stavePositionFinder: StavePositionFinder
  ): Pair<Area, Int> {
    val totalNumBars =
      entries.last().first.eventAddress.barNum + entries.last().second.getInt(EventParam.NUM_BARS) -
          entries.first().first.eventAddress.barNum + 1

    var mainArea = Area(tag = "VoltaGroup", numBars = totalNumBars)

    val startGroup =
      stavePositionFinder.getSlicePosition(entries.first().first.eventAddress)?.xMargin ?: 0

    entries.toList().forEach { (k, v) ->
      val numBars = v.getInt(EventParam.NUM_BARS)
      val endBar = if (!v.isTrue(EventParam.END))
        k.eventAddress.barNum + numBars - 1
      else {
        k.eventAddress.barNum
      }
      if (k.eventAddress.barNum > stavePositionFinder.getEndBar() ||
        endBar < stavePositionFinder.getStartBar()
      ) {
        return@forEach
      }

      val address =
        if (v.isTrue(EventParam.END)) k.eventAddress.dec(numBars - 1) else k.eventAddress

      val startVolta =
        stavePositionFinder.getBarPosition(address.barNum)?.pos
          ?: stavePositionFinder.getStartBars()
      val endVolta = getEndPos(endBar, k.eventAddress.barNum, stavePositionFinder)
      getArea(v, endVolta - startVolta)?.let { area ->
        mainArea = mainArea.addEventArea(
          v,
          area.copy(numBars = numBars),
          address.staveless(),
          Coord(startVolta - startGroup, 0),
          true
        )

      }
    }
    return Pair(mainArea, startGroup)
  }

  private fun getEndPos(endBar: Int, startBar: Int, stavePositionFinder: StavePositionFinder): Int {
    return if (stavePositionFinder.getEndBar() < endBar) {
      stavePositionFinder.getEndBars()
    } else {
      stavePositionFinder.getBarPosition(endBar)?.let {
        it.pos + it.geog.width - BLOCK_HEIGHT
      } ?: stavePositionFinder.getBarPosition(startBar)?.let {
        it.pos + it.geog.width - BLOCK_HEIGHT
      } ?: stavePositionFinder.getEndBars()
    }
  }

  private fun groupEvents(eventHash: EventHash, startBar: Int): Iterable<EventHash> {
    val currentGroup = mutableListOf<Pair<EventMapKey, Event>>()
    val groups = mutableListOf<EventHash>()

    eventHash.toList().sortedBy { it.first.eventAddress.barNum }.forEach { (emk, ev) ->

      if (emk.eventAddress.barNum >= startBar &&
        (!(ev.isTrue(EventParam.END) && ev.getInt(EventParam.NUM_BARS) > 1) ||
            emk.eventAddress.barNum - ev.getInt(EventParam.NUM_BARS) + 1 < startBar)
      ) {

        if (currentGroup.isEmpty()) {
          currentGroup.add(emk to ev)
        } else {
          val numBars = currentGroup.last().second.getParam<Int>(EventParam.NUM_BARS) ?: 1
          if (currentGroup.last().first.eventAddress.barNum + numBars == emk.eventAddress.barNum) {
            currentGroup.add(emk to ev)
          } else {
            groups.add(currentGroup.toMap())
            currentGroup.clear()
            currentGroup.add(emk to ev)
          }
        }
      }
    }
    if (currentGroup.isNotEmpty()) {
      groups.add(currentGroup.toMap())
    }
    return groups
  }

  private fun DrawableFactory.getArea(event: Event, width: Int): Area? {
    return getDrawableArea(LineArgs(width, true))?.let { horizontal ->
      getDrawableArea(LineArgs(VOLTA_HEIGHT, false))?.let { vertical ->
        event.getParam<Int>(EventParam.NUMBER)?.let { num ->
          numberArea(num, VOLTA_HEIGHT / 3 * 2)?.let { numberArea ->
            Area(
              tag = "Volta",
              event = event,
              addressRequirement = AddressRequirement.EVENT
            ).addArea(horizontal)
              .addArea(vertical).addArea(numberArea, Coord(BLOCK_HEIGHT / 2, BLOCK_HEIGHT / 2))
          }
        }
      }
    }
  }

}
