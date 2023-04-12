package com.philblandford.kscoreandroid.drawingandroid

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import com.philblandford.kscore.engine.core.area.factory.DotArgs

class AndroidDotDrawable(
  override val width: Int, override val height: Int) : AndroidDrawable(width, height) {
  override val drawable: Drawable?
    get() {
      val shape = OvalShape()
      val drawable = ShapeDrawable(shape)
      drawable.bounds = Rect(0, 0, width, height)
      return drawable
    }
}

fun androidDotDrawable(dotArgs: DotArgs): AndroidDrawable {
  return AndroidDotDrawable(dotArgs.width, dotArgs.height)
}