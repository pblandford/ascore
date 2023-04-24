package com.philblandford.kscoreandroid.drawingcompose

import android.graphics.DashPathEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.philblandford.kscore.engine.core.area.factory.LineArgs


class ComposeLineDrawable(
  override val width: Int,
  override val height: Int,
  val color: Color,
  private val horizontal: Boolean,
  private val dashSize: Int?,
  private val dashGap: Int?,
  override val export: Boolean = true, override val getDrawScope: () -> DrawScope?
) : ComposeDrawable(getDrawScope, width, height) {

  override fun DrawScope.draw(x: Int, y: Int) {
    val end = if (horizontal) {
      Offset(x.toFloat() + width, y.toFloat())
    } else {
      Offset(x.toFloat(), y.toFloat() + height)
    }
    val dashEffect = dashSize?.let { ds ->
      dashGap?.let { dg ->
        PathEffect.dashPathEffect(floatArrayOf(ds.toFloat(), dg.toFloat()), 0f)
      }
    }
    val thickness = if (horizontal) height else width
    drawLine(
      color, Offset(x.toFloat(), y.toFloat()), end, thickness.toFloat(),
      pathEffect = dashEffect
    )
  }
}


fun composeLineDrawable(
  lineArgs: LineArgs, color: Color = Color.Black,
  getDrawScope: () -> DrawScope?
): ComposeDrawable {

  return if (lineArgs.horizontal) {
    ComposeLineDrawable(
      lineArgs.length,
      lineArgs.thickness,
      color,
      lineArgs.horizontal,
      lineArgs.dashWidth,
      lineArgs.dashGap,
      lineArgs.export
    ) { getDrawScope() }
  } else {
    ComposeLineDrawable(
      lineArgs.thickness,
      lineArgs.length,
      color,
      lineArgs.horizontal,
      lineArgs.dashWidth,
      lineArgs.dashGap,
      lineArgs.export
    ) { getDrawScope() }
  }
}