package com.philblandford.kscoreandroid.drawingcompose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.philblandford.kscore.engine.core.area.factory.DiagonalArgs

class ComposeDiagonalDrawable(
  override val width: Int,
  override val height: Int,
  private val thickness: Int,
  private val up: Boolean,
  private val color:Color = Color.Black,
  override val getDrawScope: () -> DrawScope?
) : ComposeDrawable(getDrawScope, width, height) {

  override fun DrawScope.draw(x:Int, y:Int) {
    val startY = if (up) height else 0
    val endY = if (up) 0 else height
    drawLine(
      color,
      Offset(x.toFloat(), y.toFloat() + startY),
      Offset(x.toFloat() + width, y.toFloat() + endY),
      thickness.toFloat()
    )
  }
}

fun composeDiagonalDrawable(
  dotArgs: DiagonalArgs,
  color:Color = Color.Black,
  getDrawScope: () -> DrawScope?
): ComposeDrawable {
  return ComposeDiagonalDrawable(
    dotArgs.width,
    dotArgs.height,
    dotArgs.thickness,
    dotArgs.up,
    color,
    getDrawScope
  )
}
