package com.philblandford.kscoreandroid.drawingandroid

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import com.philblandford.kscore.engine.core.area.factory.LineArgs


class AndroidLineDrawable(
  override val width: Int, override val height: Int, val color: Int,
  val horizontal: Boolean, private val dashSize: Int?, private val dashGap: Int?,
  override val export:Boolean = true
) : AndroidDrawable(width, height) {
  override val drawable: Drawable? = run {

    val thickness = if (horizontal) height else width
    val path = getPath(width, height, horizontal)
    val shape = PathShape(path, width.toFloat(), height.toFloat())
    val drawable = ShapeDrawable(shape)
    val paint = drawable.paint
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = thickness.toFloat()

    dashSize?.let {
      dashGap?.let {
        paint.pathEffect = DashPathEffect(floatArrayOf(dashSize.toFloat(), dashGap.toFloat()), 0f)
      }
    }
    paint.color = color

    drawable.bounds = Rect(0, 0, width, height)
    drawable
  }
}

private fun getPath(width: Int, height: Int, horizontal: Boolean): Path {
  val path = Path()
  path.moveTo(0f, 0f);
  if (horizontal) {
    path.lineTo(width.toFloat(), 0f);
  } else {
    path.lineTo(0f, height.toFloat())
  }
  return path
}

fun androidLineDrawable(lineArgs: LineArgs): AndroidDrawable {

  return if (lineArgs.horizontal) {
    AndroidLineDrawable(
      lineArgs.length,
      lineArgs.thickness,
      lineArgs.color,
      lineArgs.horizontal,
      lineArgs.dashWidth,
      lineArgs.dashGap,
      lineArgs.export
    )
  } else {
    AndroidLineDrawable(
      lineArgs.thickness,
      lineArgs.length,
      lineArgs.color,
      lineArgs.horizontal,
      lineArgs.dashWidth,
      lineArgs.dashGap,
      lineArgs.export
    )
  }
}