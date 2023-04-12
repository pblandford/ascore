package com.philblandford.kscoreandroid.drawingcompose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.philblandford.kscoreandroid.imageKeys
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.KDrawable
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.types.isWild

private const val BlackColor = android.graphics.Color.BLACK 

class ComposeImageDrawable(
  override val width: Int,
  override val height: Int,
  private val image: ImageBitmap,
  val imageKey: String? = null,
  override val export: Boolean = true,
  private val color:Color = Color.Black,
  override val getDrawScope: () -> DrawScope?
) : ComposeDrawable(getDrawScope, width, height) {

  override fun DrawScope.draw(x:Int, y:Int) {
    drawImage(image, topLeft = Offset(x.toFloat(),y.toFloat()),
    colorFilter = ColorFilter.tint(color))
  }
}

fun composeImageDrawable(
  imageArgs: ImageArgs,
  context: Context,
  color: Color,
  getDrawScope: () -> DrawScope?
): KDrawable? {

  imageKeys[imageArgs.name]?.let { id ->
    var bm = BitmapFactory.decodeResource(context.resources, id)

    val width =
      if (imageArgs.width.isWild()) calculateWidth(bm, imageArgs.height) else imageArgs.width
    bm = Bitmap.createScaledBitmap(bm, width, imageArgs.height, false)
    var visibleWidth = width
    var visibleHeight = imageArgs.height
    imageArgs.rotation?.let {
      bm = bm.rotate(it)
      val bounds = getVisibleBounds(bm)
      visibleWidth = bounds.x
      visibleHeight = bounds.y
    }
    val asset = bm.asImageBitmap()

    return ComposeImageDrawable(
      visibleWidth,
      visibleHeight,
      asset,
      imageArgs.name,
      imageArgs.export,
      color,
      getDrawScope
    )
  } ?: run {
    throw Exception("Could not decode ${imageArgs.name}")
  }
}



private fun calculateWidth(bitmap: Bitmap, height: Int): Int {
  val ratio = bitmap.height.toFloat() / bitmap.width
  return (height.toFloat() / ratio).toInt()
}


private fun Bitmap.rotate(angle: Float): Bitmap {
  val matrix = Matrix()

  if (angle < 0) {
    matrix.postScale(1f, -1f, width.toFloat(), height.toFloat())
    matrix.postRotate(angle)
  } else {
    matrix.postRotate(angle)
  }

  return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private fun getVisibleBounds(bitmap: Bitmap): Coord {
  val width = getRealRight(bitmap) - getRealLeft(bitmap)
  val height = getRealBottom(bitmap) - getRealTop(bitmap)
  return Coord(width, height)
}

private fun getRealTop(bitmap: Bitmap): Int {
  return (0 until bitmap.height).find { y ->
    (0 until bitmap.width).any { x ->
      bitmap.getPixel(x, y) == BlackColor
    }
  } ?: 0
}

private fun getRealBottom(bitmap: Bitmap): Int {
  return (bitmap.height - 1 downTo 0).find { y ->
    (0 until bitmap.width).any { x ->
      bitmap.getPixel(x, y) == BlackColor
    }
  } ?: 0
}

private fun getRealLeft(bitmap: Bitmap): Int {
  return (0 until bitmap.width).find { x ->
    (0 until bitmap.height).any { y -> bitmap.getPixel(x, y) == BlackColor }
  } ?: 0
}

private fun getRealRight(bitmap: Bitmap): Int {
  return (bitmap.width - 1 downTo 0).find { x ->
    (0 until bitmap.height).any { y -> bitmap.getPixel(x, y) == BlackColor }
  } ?: 0
}



