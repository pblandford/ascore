package com.philblandford.kscoreandroid.drawingcompose

import Preferences
import ResourceManager
import TextFontManager
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb

import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.area.factory.TextType


class ComposeTextDrawable(
  private val drawable: Drawable,
  override val width: Int = drawable.intrinsicWidth,
  override val height: Int = drawable.intrinsicHeight,
  private val color:Color = Color.Black,
  override val getDrawScope: () -> DrawScope?
) : ComposeDrawable(
  getDrawScope,
  width,
  height
) {
  override fun DrawScope.draw(x: Int, y: Int) {

    this.drawIntoCanvas { canvas ->
      val oldBounds = drawable.copyBounds()
      drawable.bounds = Rect(x, y, x + width, y + height)
      drawable.draw(canvas.nativeCanvas)
      drawable.bounds = oldBounds
    }
  }
}

fun composeTextDrawable(
  textArgs: TextArgs,
  textFontManager: TextFontManager,
  color: Color = Color.Black,
  getDrawScope: () -> DrawScope?
): ComposeDrawable {
  val drawable =
    TextDrawable(
      textArgs.text,
      color,
      textArgs.size,
      textArgs.type,
      textArgs.font,
      textFontManager
    )
  return ComposeTextDrawable(drawable) { getDrawScope() }
}

internal class TextDrawable(
  internal val text: String, color: Color, textSize: Int, textType: TextType?,
  font: String?,
  textFontManager: TextFontManager
) : Drawable() {
  private val paint: Paint = Paint()
  private var width: Int = 0
  private var height: Int = 0


  init {
    paint.color =  color.toArgb()
    paint.textSize = textSize.toFloat()
    paint.isAntiAlias = true
    paint.isFakeBoldText = true
    paint.style = Paint.Style.FILL
    paint.textAlign = Align.LEFT
    getTypeFace(font, textType, textFontManager)?.let {
      paint.typeface = Typeface.create(it, isBold(font))
    }
    width = (paint.measureText(text, 0, text.length) + 0.5).toInt()
    height = paint.getFontMetricsInt(null)
    bounds = Rect(0, 0, width, height)
  }

  private fun isBold(name: String?): Int {
    return when (name?.lowercase()) {
      "tempo" -> Typeface.BOLD
      else -> Typeface.NORMAL
    }
  }

  private fun getTypeFace(
    font: String?, textType: TextType?,
    textFontManager: TextFontManager
  ): Typeface? {
    return textFontManager.getTextFontPath(font, textType)
      ?.let {
        Typeface.createFromFile(it)
      }
  }

  override fun draw(canvas: Canvas) {
    canvas.drawText(text, bounds.left.toFloat(), (bounds.bottom - height * 0.3).toFloat(), paint)
  }

  override fun setAlpha(alpha: Int) {
    paint.alpha = alpha
  }

  override fun setColorFilter(cf: ColorFilter?) {
    paint.colorFilter = cf
  }

  override fun getOpacity(): Int {
    return paint.alpha
  }

  override fun getIntrinsicWidth(): Int {
    return width
  }

  override fun getIntrinsicHeight(): Int {
    return height
  }
}

private fun getDefaultForType(textType: TextType): String {
  return when (textType) {
    TextType.SYSTEM -> "tempo"
    TextType.EXPRESSION -> "expression"
    else -> "default"
  }
}