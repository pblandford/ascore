package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.SegmentGeography
import com.philblandford.kscore.engine.core.SlicePosition
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DiagonalArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.GLISSANDO_HEIGHT
import com.philblandford.kscore.engine.core.representation.LINE_THICKNESS
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.util.toDegrees
import kotlin.math.*

object GlissandoDecorator : Decorator {
  override fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area, drawableFactory: DrawableFactory
  ): Area {
    return eventHash.toList().filterNot { it.second.isTrue(EventParam.END) }
      .fold(staveArea) { sa, (k, v) ->
        drawableFactory.addArea(sa, v, k.eventAddress, stavePositionFinder)
      }
  }


  private fun DrawableFactory.addArea(
    main: Area, event: Event, eventAddress: EventAddress,
    stavePositionFinder: StavePositionFinder
  ): Area {
    return stavePositionFinder.getSlicePosition(eventAddress)?.let { start ->
      stavePositionFinder.getSegmentGeography(eventAddress)?.let { geog ->
        event.getParam<Duration>(EventParam.DURATION)?.let { duration ->
          stavePositionFinder.getOffsetLookup().addDuration(eventAddress, duration)?.let { end ->
            val endSlice = stavePositionFinder.getSlicePosition(end)
            val endGeog = endSlice?.let { stavePositionFinder.getSegmentGeography(end) }
            addGlissandoArea(main, start, endSlice, geog, endGeog, event, eventAddress)
          }
        }
      }
    } ?: main
  }

  private fun DrawableFactory.addGlissandoArea(
    main: Area,
    startSlice: SlicePosition, endSlice: SlicePosition?, startGeog: SegmentGeography,
    endGeog: SegmentGeography?, event: Event, eventAddress: EventAddress
  ): Area {
    val up = startGeog.topNote > endGeog?.bottomNote ?: BLOCK_HEIGHT * 4
    val startX = startSlice.xMargin + startGeog.width - startGeog.xMargin + BLOCK_HEIGHT
    val width =
      endSlice?.let { it.start - startX - BLOCK_HEIGHT + event.getInt(EventParam.EXTRA_WIDTH) }
        ?: startSlice.width
    val startY = if (!up) startGeog.topNote else startGeog.bottomNote
    val endY = endGeog?.let {
      if (!up) endGeog.bottomNote else endGeog.topNote
    } ?: if (up) startY - BLOCK_HEIGHT * 6 else startY + BLOCK_HEIGHT * 6
    val height = abs(endY - startY)
    val top = min(startY, endY)
    return glissandoArea(event, width, height, up)?.let {
      main.addEventArea(event, it, eventAddress, Coord(startX, top))
    } ?: main
  }

  private fun DrawableFactory.glissandoArea(
    event: Event,
    width: Int,
    height: Int,
    up: Boolean
  ): Area? {
    return when (event.getParam<GlissandoType>(EventParam.TYPE)) {
      GlissandoType.LINE -> glissandoAreaLine(event, width, height, up)
      else -> glissandoAreaWavy(event, width, height, up)
    }
  }

  private fun DrawableFactory.glissandoAreaLine(
    event: Event,
    width: Int,
    height: Int,
    up: Boolean
  ): Area? {
    return getDrawableArea(DiagonalArgs(width, height, LINE_THICKNESS, up))?.copy(
      tag = "Glissando",
      event = event
    )
  }

  private fun DrawableFactory.glissandoAreaWavy(
    event: Event,
    width: Int,
    height: Int,
    up: Boolean
  ): Area? {

    return getLinePart(width, height, up)?.let { pair ->
      val area = pair.first
      val dimen = pair.second

      val mainArea = Area(tag = "Glissando", event = event)

      var yPos = if (up) height + dimen.y else 0
      val yStep = dimen.y

      return ((0 until width - dimen.x) step dimen.x).fold(mainArea) { ma, xPos ->
        val y = yPos
        yPos += yStep
        ma.addArea(area, Coord(xPos, y))
      }
    }
  }

  private fun DrawableFactory.glissPartStraightWidth() =
    getDrawableArea(ImageArgs("glissando_part", INT_WILD, GLISSANDO_HEIGHT))?.width
      ?: GLISSANDO_HEIGHT

  private fun DrawableFactory.getLinePart(
    width: Int,
    height: Int,
    up: Boolean
  ): Pair<Area, Coord>? {
    var angleR = atan(height.toFloat() / width.toFloat())
    if (up) angleR = -angleR
    return getDrawableArea(
      ImageArgs(
        "glissando_part",
        INT_WILD,
        GLISSANDO_HEIGHT,
        angleR.toDegrees()
      )
    )?.copy(tag = "GlissPart")?.let { linePart ->
      val vWidth = cos(angleR) * glissPartStraightWidth()
      val vHeight = sin(angleR) * glissPartStraightWidth()

      Pair(linePart, Coord(vWidth.toInt(), vHeight.toInt()))
    }
  }
}