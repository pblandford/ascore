package com.philblandford.kscoreandroid.drawingandroid

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import com.philblandford.kscore.engine.core.area.factory.RectArgs

class AndroidRectDrawable(
  override val width: Int, override val height: Int,
  val fill: Boolean, val color: Int, val thickness:Int
) : AndroidDrawable(width, height) {
  override val drawable: Drawable?
    get() {
      val shape = RectShape()
      val drawable = ShapeDrawable(shape)
      drawable.paint.style = if (fill) Paint.Style.FILL else Paint.Style.STROKE
      drawable.paint.strokeWidth = thickness.toFloat()
      drawable.paint.color = color
      drawable.bounds = Rect(0, 0, width, height)
      return drawable
    }
}

fun androidRectDrawable(rectArgs: RectArgs): AndroidDrawable {
  return AndroidRectDrawable(rectArgs.width, rectArgs.height, rectArgs.fill, rectArgs.color, rectArgs.thickness)
}