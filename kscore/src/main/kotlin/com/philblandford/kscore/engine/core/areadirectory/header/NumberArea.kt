package com.philblandford.kscore.engine.core.areadirectory.header

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.types.INT_WILD

fun DrawableFactory.numberArea(num:Int, height:Int): Area? {
  var area = Area(tag = "Number-$num")

  num.toString().forEach { digit ->
    getDrawableArea(ImageArgs(imageKeys[digit.toString().toInt()], INT_WILD, height))?.let {
      area = area.addRight(it, gap = BLOCK_HEIGHT/4)
    }
  }

  return area
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