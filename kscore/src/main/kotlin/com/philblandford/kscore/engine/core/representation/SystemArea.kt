package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.engine.core.PartPosition
import com.philblandford.kscore.engine.core.SystemYGeography
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.types.*

fun DrawableFactory.systemArea(
  partQuery: PartQuery,
  systemYGeography: SystemYGeography,
  staveJoins: EventHash
): Area {
  var area =
    Area(tag = "System", extra = systemYGeography, numBars = systemYGeography.xGeog.numBars)
  val parts = partQuery.getParts()
  val barNum = systemYGeography.xGeog.startBar

  systemYGeography.partPositions.forEach { entry ->
    entry.value?.let { partPosition ->
      parts[EventAddress(barNum, staveId = StaveId(entry.key, 0))]?.let { partArea ->
        area = area.addArea(
          partArea.base, Coord(0, partPosition.pos),
          ez(barNum).copy(staveId = StaveId(entry.key, 0))
        )
      }
    }
  }
  area = addStartBarLine(area, systemYGeography)
  area = addStaveJoins(area, systemYGeography, staveJoins)
  return area
}

private fun DrawableFactory.addStartBarLine(area: Area, systemYGeography: SystemYGeography): Area {
  return systemYGeography.partPositions.toList().sortedBy { it.first }.find { it.second != null }?.let { first ->
    systemYGeography.partPositions.toList().sortedBy { it.first }.findLast { it.second != null }
      ?.let { last ->
        first.second?.let { startPos ->
          last.second?.let { endPos ->
            val start = startPos.pos
            val lastStave =
              endPos.partGeography.stavePositions.toList().sortedBy { it.first }.last().second.pos
            val end = endPos.pos + lastStave + STAVE_HEIGHT

            val barLine = getDrawableArea(LineArgs(end - start, false))?.copy(
              tag = "SystemBarLine",
              event = Event(EventType.BARLINE, paramMapOf())
            ) ?: Area()
            area.addArea(
              barLine,
              Coord(systemYGeography.xGeog.preHeaderLen, start),
              ez(systemYGeography.xGeog.startBar)
            )
          }
        }
      }
  } ?: area
}

private fun DrawableFactory.addStaveJoins(
  area: Area,
  systemYGeography: SystemYGeography,
  staveJoins: EventHash
): Area {

  var copy = area
  staveJoins.forEach { (k, v) ->
    systemYGeography.partPositions[k.eventAddress.staveId.main]?.let { top ->
      copy = doAddJoin(v, k.eventAddress, top, systemYGeography, copy)
    }
  }
  return copy
}

private fun DrawableFactory.doAddJoin(
  event: Event, eventAddress: EventAddress, topPos: PartPosition,
  systemYGeography: SystemYGeography, area: Area
): Area {
  return event.getParam<Int>(EventParam.NUMBER)?.let { num ->
    if (num == 1 && systemYGeography.partPositions[eventAddress.staveId.main]?.partGeography?.stavePositions?.size == 1) {
      area
    } else {
      systemYGeography.partPositions[eventAddress.staveId.main + num - 1]?.let { bottom ->
        val bottomStave = bottom.partGeography.stavePositions.toList().last().second
        val height = bottom.pos + bottomStave.pos + STAVE_HEIGHT - topPos.pos
        getArea(height, event)?.let { joinArea ->
          val offset =
            if (event.subType == StaveJoinType.BRACKET) joinArea.width / 2 else joinArea.width
          val xPos = systemYGeography.xGeog.preHeaderLen - offset - LINE_THICKNESS
          area.addArea(joinArea, Coord(xPos, topPos.pos), eventAddress)
        }
      }
    }
  } ?: area
}

private fun DrawableFactory.getArea(height: Int, event: Event): Area? {
  return when (event.subType) {
    StaveJoinType.BRACKET -> staveJoinArea(height, event)
    StaveJoinType.GRAND -> getDrawableArea(ImageArgs("join_grand", STAVE_JOIN_THICKNESS * 3, height))?.copy(
      tag = "StaveJoin", event = event, addressRequirement = AddressRequirement.EVENT
    )
    else -> null
  }
}

