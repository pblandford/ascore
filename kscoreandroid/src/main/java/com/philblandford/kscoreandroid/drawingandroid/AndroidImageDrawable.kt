package com.philblandford.kscoreandroid.drawingandroid

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import com.philblandford.kscoreandroid.imageKeys
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.KDrawable
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.types.isWild
import com.philblandford.kscore.log.ksLogt

class AndroidImageDrawable(
  override val width: Int,
  override val height: Int,
  private val bitmap: Bitmap,
  val imageKey: String? = null,
  override val export: Boolean = true
) : AndroidDrawable(width, height) {
  override val drawable: Drawable?
    get() = null
  private val paint = Paint()

  override fun draw(x:Int, y:Int, export: Boolean, vararg args:Any) {
    if (export && !this.export) {
      return
    }

    if (imageKey == "glissando_part") {
      ksLogt("Here")
    }
    val canvas = args[0] as Canvas
    canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), paint)
  }
}

fun androidImageDrawable(context: Context, imageArgs: ImageArgs): KDrawable? {

  imageKeys[imageArgs.name]?.let { id ->
    val bitmap = BitmapFactory.decodeResource(context.resources, id)
    val realWidth =
      if (imageArgs.width.isWild()) calculateWidth(bitmap, imageArgs.height) else imageArgs.width
    var scaledBitmap = Bitmap.createScaledBitmap(bitmap, realWidth, imageArgs.height, false)
    var visibleWidth = realWidth
    var visibleHeight = imageArgs.height
    scaledBitmap = imageArgs.rotation?.let {
      val bm = rotate(scaledBitmap, it)
      val bounds = getVisibleBounds(bm)
      visibleWidth = bounds.x
      visibleHeight = bounds.y
      bm
    } ?: scaledBitmap
    return AndroidImageDrawable(
      visibleWidth,
      visibleHeight,
      scaledBitmap,
      imageArgs.name,
      imageArgs.export
    )
  } ?: run {
    throw Exception("Could not decode ${imageArgs.name}")
  }
}


private fun calculateWidth(bitmap: Bitmap, height: Int): Int {
  val ratio: Float = bitmap.height.toFloat() / bitmap.width
  return (height.toFloat() / ratio).toInt()
}

private fun rotate(bitmap: Bitmap, angle: Float): Bitmap {
  val matrix = Matrix()

  if (angle < 0) {
    matrix.postScale(1f, -1f, bitmap.width.toFloat(), bitmap.height.toFloat())
    matrix.postRotate(angle)
  } else {
    matrix.postRotate(angle)
  }

  return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun getVisibleBounds(bitmap: Bitmap): Coord {
  val width = getRealRight(bitmap) - getRealLeft(bitmap)
  val height = getRealBottom(bitmap) - getRealTop(bitmap)
  return Coord(width, height)
}

private fun getRealTop(bitmap: Bitmap): Int {
  return (0 until bitmap.height).find { y ->
    (0 until bitmap.width).any { x ->
      bitmap.getPixel(x, y) == Color.BLACK
    }
  } ?: 0
}

private fun getRealBottom(bitmap: Bitmap): Int {
  return (bitmap.height - 1 downTo 0).find { y ->
    (0 until bitmap.width).any { x ->
      bitmap.getPixel(x, y) == Color.BLACK
    }
  } ?: 0
}

private fun getRealLeft(bitmap: Bitmap): Int {
  return (0 until bitmap.width).find { x ->
    (0 until bitmap.height).any { y -> bitmap.getPixel(x, y) == Color.BLACK }
  } ?: 0
}

private fun getRealRight(bitmap: Bitmap): Int {
  return (bitmap.width - 1 downTo 0).find { x ->
    (0 until bitmap.height).any { y -> bitmap.getPixel(x, y) == Color.BLACK }
  } ?: 0
}



