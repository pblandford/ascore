package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.engine.core.PageGeography
import com.philblandford.kscore.engine.core.SystemYGeography
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.AreaMapKey
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.core.area.factory.RectArgs
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.StaveId
import com.philblandford.kscore.engine.types.ez
import com.philblandford.kscore.engine.util.blue
import com.philblandford.kscore.engine.util.lightGrey
import com.philblandford.kscore.engine.util.selectionColor
import com.philblandford.kscore.log.ksLogt
import com.philblandford.kscore.select.SelectState


internal fun DrawableFactory.paintSelection(page: Area, selectState: SelectState?): Area {
  if (selectState?.area != null) {
    return page
  }
  var copy = page

  selectState?.start?.let { startSelect ->
    val endSelect = selectState.end ?: startSelect

    val systems = page.findByTag("System")

    systems.forEach { (_, system) ->
      val geog = system.extra as SystemYGeography
      val startBar = geog.xGeog.startBar
      val endBar = geog.xGeog.endBar
      if ((startSelect.barNum in startBar..endBar) ||
        (startSelect.barNum <= startBar && endSelect.barNum >= endBar) ||
        (endSelect.barNum in startBar..endBar)
      ) {
        val startPaint = if (startSelect.barNum >= startBar) startSelect
        else ez(startBar).copy(staveId = startSelect.staveId)
        val endPaint = if (endSelect.barNum <= endBar) endSelect
        else getLastSegment(system, endSelect.staveId)?.copy(staveId = endSelect.staveId)
          ?: endSelect
        copy = paintRange(copy, startPaint, endPaint)
      }
    }
  }
  return copy
}

private fun getLastSegment(system: Area, staveId: StaveId): EventAddress? {
  return system.findByTag("Part").toList()
    .find { it.first.eventAddress.staveId.main == staveId.main }?.let { (_, part) ->
      part.findByTag("Stave").toList().find { it.first.eventAddress.staveId.sub == staveId.sub }
        ?.let { (_, stave) ->
          stave.findByTag("Segment").toList().maxByOrNull { it.first.eventAddress }
            ?.first?.eventAddress
        }
    }
}

private fun DrawableFactory.paintRange(page: Area, start: EventAddress, end: EventAddress): Area {
  return page.getArea(start.voiceless()) { it.addressRequirement == AddressRequirement.SEGMENT }
    ?.let { (startKey, _) ->
      page.getArea(end.voiceless()) { it.addressRequirement == AddressRequirement.SEGMENT }
        ?.let { (endKey, endArea) ->

          val startX = startKey.coord.x
          val endX = endKey.coord.x + endArea.width - endArea.xMargin
          val startY = startKey.coord.y
          val endY = endKey.coord.y + STAVE_HEIGHT

          getDrawableArea(
            RectArgs(endX - startX, endY - startY, true, selectionColor())
          )?.let { rect ->
            page.addArea(rect, Coord(startX, startY))
          }
        }
    } ?: page
}

internal fun DrawableFactory.paintPlayBackMarker(
  page: Area,
  pageGeography: PageGeography,
  playbackMarker: EventAddress?
): Area {
  return playbackMarker?.let { marker ->
    pageGeography.getSlicePositionAt(marker)?.let { coord ->
      getBottomSystem(marker, pageGeography)?.let { bottom ->
        getDrawableArea(LineArgs(bottom - coord.y, false, LINE_THICKNESS, blue()))?.let { line ->
          page.addArea(line, Coord(coord.x, coord.y - STAVE_HEIGHT))
        }
      }
    }
  } ?: page
}

internal fun DrawableFactory.paintSelectedArea(
  page: Area,
  pageGeography: PageGeography,
  selectState: SelectState?
): Area {
  return selectState?.area?.let { ats ->
    if ((ats.scoreArea.page == pageGeography.pageNum)) {
      getDrawableArea(
        RectArgs(ats.width, ats.height, false, selectionColor(), 16)
      )?.let { rect ->
        page.addArea(rect, Coord(ats.x, ats.y))
      }
    } else null
  } ?: page
}

internal fun Representation.paintMarker(
  area: Area,
  pageGeography: PageGeography,
  marker: EventAddress?,
  df: DrawableFactory
): Area {
  return marker?.let { ea ->
    if (thisPage(pageGeography, ea)) {
      getArea(AddressRequirement.SEGMENT, ea)?.let { areaReturn ->
        df.getDrawableArea(LineArgs(STAVE_HEIGHT, false, LINE_THICKNESS * 2))?.let { line ->
          area.addArea(
            line,
            Coord(
              areaReturn.x - BLOCK_HEIGHT / 2,
              areaReturn.y - BLOCK_HEIGHT * 2
            )
          )
        }
      }
    } else area
  } ?: area
}

internal fun Area.paintBorders(
  drawableFactory: DrawableFactory,
  condition: (AreaMapKey, Area) -> Boolean = { _, _ -> true },
): Area {
  return childMap.toList().fold(this) { thisArea, (key, child) ->
    val painted = if (condition(key, child)) {
      drawableFactory.getDrawableArea(RectArgs(child.width, child.height, false, blue()))
        ?.let { borderArea ->
          thisArea.addArea(borderArea, key.coord)
        } ?: thisArea
    } else {
      thisArea
    }
    painted.replaceArea(key, child.paintBorders(drawableFactory, condition))
  }
}

internal fun Area.paintGrid(df: DrawableFactory): Area {

  var newBase = (0..height step 1000).fold(this) { a, y ->
    df.getDrawableArea(LineArgs(width, true, color = blue()))?.let {
      a.addArea(it, Coord(0, y))
    } ?: a
  }
  newBase = (0..width step 1000).fold(newBase) { a, x ->
    df.getDrawableArea(LineArgs(height, false, color = blue()))?.let {
      a.addArea(it, Coord(x, 0))
    } ?: a
  }
  return newBase
}

private fun thisPage(pageGeography: PageGeography, address: EventAddress): Boolean {
  return (pageGeography.startBar <= address.barNum && pageGeography.endBar >= address.barNum)
}