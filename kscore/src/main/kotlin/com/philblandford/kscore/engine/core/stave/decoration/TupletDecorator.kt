package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.core.areadirectory.header.numberArea
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.TADPOLE_WIDTH
import com.philblandford.kscore.engine.core.representation.TUPLET_NUMBER_HEIGHT
import com.philblandford.kscore.engine.core.score.Tuplet
import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.StavePositionFinder

object TupletDecorator : Decorator {

  override fun decorate(
    eventHash: EventHash,
    stavePositionFinder: StavePositionFinder,
    staveArea: Area, drawableFactory: DrawableFactory
  ): Area {
    var staveCopy = staveArea

    val filtered =
      eventHash.filterNot {
        it.value.isTrue(EventParam.END) || it.value.isTrue(EventParam.HIDDEN)
      }

    filtered.forEach { (key, event) ->
      tuplet(event)?.let { tuplet ->
        stavePositionFinder.getSlicePosition(key.eventAddress)?.let { start ->
          event.getParam<Iterable<Offset>>(EventParam.MEMBERS)?.let { members ->

            stavePositionFinder.getSlicePosition(
              key.eventAddress.copy(
                offset = members.maxOrNull() ?: dZero()
              )
            )
              ?.let { end ->
                val above = key.eventAddress.voice == 1
                val width = end.xMargin + TADPOLE_WIDTH * 2 - start.xMargin

                drawableFactory.tupletArea(tuplet, width, above)?.let { area ->
                  val top = getTop(start.xMargin, end.xMargin, staveCopy, above)
                  val yPos = if (above) {
                    top - area.height - BLOCK_HEIGHT
                  } else {
                    top + BLOCK_HEIGHT
                  }
                  area.height - BLOCK_HEIGHT
                  staveCopy = staveCopy.addEventArea(
                      event,
                      area,
                      key.eventAddress,
                      Coord(start.xMargin, yPos)
                    )
                }

              }
          }
        }
      }
    }
    return staveCopy
  }

  private fun getTop(startX: Int, endX: Int, staveArea: Area, above: Boolean): Int {
    return if (above) {
      staveArea.getTopForRange(startX, endX)
    } else {
      staveArea.getBottomForRange(startX, endX)
    }
  }

  private fun DrawableFactory.tupletArea(tuplet: Tuplet, width: Int, up: Boolean): Area? {
    return numberArea(tuplet.timeSignature.numerator, TUPLET_NUMBER_HEIGHT)?.let { num ->
      var base = Area(
        tag = "Tuplet", width = width, event = tuplet.toEvent(),
        addressRequirement = AddressRequirement.EVENT
      )
      val y = if (up) -num.height / 2 else num.height / 2
      base = base.addArea(num, Coord(width / 2 - num.width / 2, y))

      if (showLines(tuplet)) {
        leftLine(width, num.width, up)?.let { leftLine ->
          rightLine(width, num.width, up)?.let { rightLine ->

            base = base.addArea(leftLine)
            base = base.addArea(rightLine, Coord(width - rightLine.width))
          }
        }
      }
      base
    }
  }

  private fun showLines(tuplet: Tuplet): Boolean {
    return tuplet.timeSignature.denominator <= crotchet().denominator
  }

  private fun DrawableFactory.leftLine(width: Int, numberWidth: Int, up: Boolean): Area? {
    return getDrawableArea(LineArgs(TUPLET_NUMBER_HEIGHT, false))?.let { vertical ->
      getDrawableArea(
        LineArgs(
          width / 2 - numberWidth - BLOCK_HEIGHT,
          true
        )
      )?.let { horizontal ->
        val y = if (up) 0 else vertical.height
        Area(tag = "TupletLine").addArea(vertical).addArea(horizontal, Coord(0, y))
      }
    }
  }

  private fun DrawableFactory.rightLine(width: Int, numberWidth: Int, up: Boolean): Area? {
    val lineWidth = width / 2 - numberWidth - BLOCK_HEIGHT
    return getDrawableArea(LineArgs(TUPLET_NUMBER_HEIGHT, false))?.let { vertical ->
      getDrawableArea(
        LineArgs(
          lineWidth,
          true
        )
      )?.let { horizontal ->
        val y = if (up) 0 else vertical.height
        Area(tag = "TupletLine").addArea(vertical, Coord(lineWidth, 0))
          .addArea(horizontal, Coord(0, y))
      }
    }
  }
}