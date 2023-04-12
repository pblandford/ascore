package com.philblandford.kscoreandroid.drawingandroid

import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import com.philblandford.kscore.engine.core.area.factory.DiagonalArgs


class AndroidDiagonalDrawable(
  override val drawable: Drawable,
  override val width: Int, override val height: Int
) : AndroidDrawable(width, height) {
}

fun androidDiagonalDrawable(diagonalArgs: DiagonalArgs): AndroidDrawable {
  val path = getPath(diagonalArgs.width, diagonalArgs.height, diagonalArgs.up)
  val drawable = ShapeDrawable(
    PathShape(
      path, diagonalArgs.width.toFloat(),
      diagonalArgs.height.toFloat() + diagonalArgs.thickness
    )
  )
  drawable.bounds = Rect(0, 0, diagonalArgs.width, diagonalArgs.height + diagonalArgs.thickness)
  setPaint(drawable, diagonalArgs.thickness)
  return AndroidDiagonalDrawable(drawable, diagonalArgs.width, diagonalArgs.height + diagonalArgs.thickness)
}

private fun setPaint(drawable: ShapeDrawable, thickness: Int) {
  val paint = drawable.paint
  paint.style = Paint.Style.STROKE
  paint.strokeWidth = thickness.toFloat()
}

private fun getPath(width: Int, height: Int, up: Boolean): Path {
  val path = Path()
  if (up) {
    path.moveTo(0f, height.toFloat())
    path.lineTo(width.toFloat(), 0f)
  } else {
    path.moveTo(0f, 0f)
    path.lineTo(width.toFloat(), height.toFloat())
  }
  return path
}

