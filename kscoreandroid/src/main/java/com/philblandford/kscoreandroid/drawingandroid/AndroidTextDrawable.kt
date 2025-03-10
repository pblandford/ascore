package com.philblandford.kscoreandroid.drawingandroid

import TextFontManager
import android.graphics.*
import android.graphics.Paint.Align
import android.graphics.drawable.Drawable
import com.philblandford.kscore.engine.core.area.factory.TextArgs


class AndroidTextDrawable(override val drawable: Drawable?) : AndroidDrawable(
  drawable?.intrinsicWidth ?: 0,
  drawable?.intrinsicHeight ?: 0
)

fun androidTextDrawable(
  textArgs: TextArgs,
  textFontManager: TextFontManager
): AndroidDrawable {
  val typeFace = getTypeFace(textArgs.font, textFontManager)?.let {
    Typeface.create(
      it,
      isBold(textArgs.font)
    )
  }

  val drawable: Drawable? =
    TextDrawable(
      textArgs.text,
      textArgs.color,
      textArgs.size,
      textArgs.font,
      typeFace
    )
  return AndroidTextDrawable(drawable)
}

private fun getTypeFace(name: String?, textFontManager: TextFontManager): Typeface? {
  return textFontManager.getTextFontPath(name ?: "default")?.let { Typeface.createFromFile(it) }
}

private fun isBold(name: String?): Int {
  return when (name?.lowercase()) {
    "tempo" -> Typeface.BOLD
    else -> Typeface.NORMAL
  }
}



internal class TextDrawable(
  internal val text: String, color: Int, textSize: Int, font: String?,
  typeface: Typeface?
) : Drawable() {
  private val paint: Paint = Paint()
  private var width: Int = 0
  private var height: Int = 0


  init {
    paint.color = color
    paint.textSize = textSize.toFloat()
    paint.isAntiAlias = true
    paint.isFakeBoldText = true
    paint.style = Paint.Style.FILL
    paint.textAlign = Align.LEFT
    paint.typeface = typeface
    width = (paint.measureText(text, 0, text.length) + 0.5).toInt()
    height = paint.getFontMetricsInt(null)
    bounds = Rect(0, 0, width, height)
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

