package com.philblandford.kscoreandroid.drawingcompose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.philblandford.kscore.engine.core.area.factory.RectArgs

class ComposeRectDrawable(
  override val width: Int, override val height: Int,
  val fill: Boolean, val color: Color, val thickness: Int,
  getDrawScope: () -> DrawScope?
) : ComposeDrawable(getDrawScope, width, height) {

  override fun DrawScope.draw(x:Int, y:Int) {
    drawRect(
      color, Offset(x.toFloat(), y.toFloat()), Size(width.toFloat(), height.toFloat()),
      style = if (fill) Fill else Stroke(thickness.toFloat())
    )
  }
}

fun composeRectDrawable(rectArgs: RectArgs, getDrawScope: () -> DrawScope?): ComposeDrawable {
  return ComposeRectDrawable(
    rectArgs.width,
    rectArgs.height,
    rectArgs.fill,
    Color(rectArgs.color),
    rectArgs.thickness,
    getDrawScope
  )
}