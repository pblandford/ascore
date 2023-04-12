package com.philblandford.kscoreandroid.drawingandroid

import ResourceManager
import TextFontManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.philblandford.kscore.api.DrawableGetter
import com.philblandford.kscore.engine.core.area.KDrawable
import com.philblandford.kscore.engine.core.area.factory.*
import java.io.File
import java.util.prefs.Preferences


private const val FONT_DIR = "Fonts"

abstract class AndroidDrawable(
  override val width: Int, override val height: Int,
  override val effectiveHeight: Int = height,
  override val trim: Int = 0,
  override val export: Boolean = true
) : KDrawable {
  abstract val drawable: Drawable?

  override fun draw(x:Int, y:Int, export: Boolean, vararg args: Any) {
    if (export && !this.export) {
      return
    }
    val canvas = args[0] as Canvas
    drawable?.let { drawable ->
      val oldBounds = drawable.copyBounds()
      drawable.bounds = Rect(x, y, x + width, y + height)
      drawable.draw(canvas)
      drawable.bounds = oldBounds
    }
  }
}

class AndroidDrawableGetter(
  private val context: Context,
  private val textFontManager: TextFontManager
) : DrawableGetter {

  private lateinit var canvas: Canvas

  fun prepare(c: Canvas) {
    canvas = c
  }

  override fun prepare(vararg args: Any) {
    canvas = args[0] as Canvas
  }

  override fun getDrawable(drawableArgs: DrawableArgs): KDrawable? {
    return when (drawableArgs) {
      is BeamArgs -> androidBeamDrawable(
        drawableArgs
      )
      is DiagonalArgs -> androidDiagonalDrawable(drawableArgs)
      is LineArgs -> androidLineDrawable(drawableArgs)
      is RectArgs -> androidRectDrawable(drawableArgs)
      is SlurArgs -> androidSlurDrawable(drawableArgs)
      is TextArgs -> androidTextDrawable(drawableArgs, textFontManager)
      is DotArgs -> androidDotDrawable(drawableArgs)
      is ImageArgs -> context.let { androidImageDrawable(it, drawableArgs) }
    }
  }

  override fun getDrawArgs(): Array<Any> {
    return arrayOf(canvas)
  }

  private fun getFontDir(): File? {
    return context.filesDir?.let { files ->
      val dir = File(files, FONT_DIR)
      if (!dir.exists()) {
        dir.mkdirs()
      }
      dir
    }
  }
}