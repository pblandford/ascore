package com.philblandford.kscore.engine.core.areadirectory.header

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.Blocks
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.INT_WILD
import com.philblandford.kscore.engine.types.TimeSignatureType.COMMON
import com.philblandford.kscore.engine.types.TimeSignatureType.CUSTOM
import kotlin.math.max

fun DrawableFactory.timeSignatureArea(timeSignature: TimeSignature): Area {
  val area = Area(tag = "TimeSignature",event = timeSignature.toEvent())

  return when (timeSignature.type) {
    CUSTOM -> createCustom(area, timeSignature)
    else -> createImage(area, timeSignature)
  }
}

private fun DrawableFactory.createCustom(area:Area, timeSignature: TimeSignature): Area {
  return numberArea(timeSignature.numerator, BLOCK_HEIGHT * 4)?.let { numArea ->
    numberArea(timeSignature.denominator, BLOCK_HEIGHT * 4)?.let { denArea ->
      val totalWidth = max(numArea.width, denArea.width)
      area.addArea(numArea, Coord(totalWidth / 2 - numArea.width / 2))
        .addBelow(denArea, x = totalWidth / 2 - denArea.width / 2)
    }
  } ?: area
}

private data class Desc(val key:String, val offset:Blocks, val height:Blocks)
private fun DrawableFactory.createImage(area:Area, timeSignature: TimeSignature): Area {
  val desc = if (timeSignature.type == COMMON) Desc("time_signature_common", 2, 4)
  else Desc("time_signature_cut_common", 1, 6)
  return getDrawableArea(ImageArgs(desc.key, INT_WILD, desc.height* BLOCK_HEIGHT))?.let { sub ->
    area.addArea(sub, Coord(0, desc.offset* BLOCK_HEIGHT))
  } ?: area

}

private val imageKeys = listOf(
  "number_zero",
  "number_one",
  "number_two",
  "number_three",
  "number_four",
  "number_five",
  "number_six",
  "number_seven",
  "number_eight",
  "number_nine"
)