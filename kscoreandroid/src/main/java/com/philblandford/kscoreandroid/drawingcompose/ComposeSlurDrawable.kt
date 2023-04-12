package com.philblandford.kscoreandroid.drawingcompose

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.SlurArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.LINE_THICKNESS
import com.philblandford.kscore.engine.core.representation.SLUR_THICKNESS
import com.philblandford.kscore.log.ksLoge
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val BEZIER_CONTROL_GUESS = BLOCK_HEIGHT

class ComposeSlurDrawable(
  override val width: Int, override val height: Int, val up: Boolean,
  heightDiff: Int, private val drawable: Drawable,
  private val color: Color,
  override val getDrawScope: () -> DrawScope?
) : ComposeDrawable(getDrawScope, width, height) {

  override val trim = if (up) heightDiff else 0
  override val effectiveHeight: Int = height - heightDiff

  override fun DrawScope.draw(x: Int, y: Int) {
    this.drawIntoCanvas { canvas ->
      val oldBounds = drawable.copyBounds()
      drawable.bounds = Rect(x, y, x + width, y + height)
      drawable.draw(canvas.nativeCanvas)
      drawable.bounds = oldBounds
    }
  }

  override fun draw(x: Int, y: Int, export: Boolean, vararg args: Any) {
    if (up) {
      val startY = if (up) -BEZIER_CONTROL_GUESS else 0
      super.draw(x, y + startY, export)
    } else {
      super.draw(x, y, export)
    }
  }
}

fun composeSlurDrawable(
  slurArgs: SlurArgs,
  color: Color = Color.Black,
  getDrawScope: () -> DrawScope?
): ComposeDrawable {
  val width = slurArgs.end.x - slurArgs.start.x
  val height = getHeight(slurArgs)
  val drawable = getDrawable(slurArgs.start, slurArgs.mid, slurArgs.end, slurArgs.up, width, height)
  val vTopBottom = getVisibleTopBottom(slurArgs, width, height, drawable, slurArgs.up)

  val heightDiff = if (slurArgs.up) vTopBottom.first else height - vTopBottom.second

  return ComposeSlurDrawable(width, height, slurArgs.up, heightDiff, drawable, color, getDrawScope)
}

private fun getHeight(slurArgs: SlurArgs): Int {
  val mid = if (slurArgs.up) slurArgs.mid.y - BEZIER_CONTROL_GUESS
  else slurArgs.mid.y + BEZIER_CONTROL_GUESS
  val low = max(max(slurArgs.start.y, mid), slurArgs.end.y)
  val high = min(min(slurArgs.start.y, mid), slurArgs.end.y)
  return abs(high - low)
}

private fun getDrawable(
  start: Coord, mid: Coord, end: Coord, up: Boolean,
  width: Int, height: Int
): Drawable {
  val path = getPath(start, mid, end, up)
  val shapeDrawable = ShapeDrawable(PathShape(path, width.toFloat(), height.toFloat()))
  val paint: Paint = shapeDrawable.paint
  paint.style = Paint.Style.STROKE
  paint.strokeWidth = SLUR_THICKNESS.toFloat()
  paint.strokeCap = Paint.Cap.ROUND
  paint.isAntiAlias = true
  shapeDrawable.setBounds(0, 0, width, height)
  return shapeDrawable
}

private fun getPath(start: Coord, mid: Coord, end: Coord, up: Boolean): Path {
  val adjustedMid =
    if (up) mid.plusY(-BEZIER_CONTROL_GUESS) else mid.plusY(BEZIER_CONTROL_GUESS)
  val list = listOf(start, adjustedMid, end)
  val topCoord = list.minByOrNull { it.y }!!

  val startRel = Coord(0, if (start == topCoord) 0 else start.y - topCoord.y)
  val midRel =
    Coord(adjustedMid.x - start.x, if (adjustedMid == topCoord) 0 else adjustedMid.y - topCoord.y)
  val endRel = Coord(end.x - start.x, if (end == topCoord) 0 else end.y - topCoord.y)


  val path = Path()
  path.moveTo(startRel.x.toFloat(), startRel.y.toFloat())
  path.quadTo(midRel.x.toFloat(), midRel.y.toFloat(), endRel.x.toFloat(), endRel.y.toFloat())
  path.moveTo(startRel.x.toFloat(), startRel.y.toFloat())
  val thickBitMidPointY = midRel.y + LINE_THICKNESS * 2
  path.quadTo(
    midRel.x.toFloat(),
    thickBitMidPointY.toFloat(),
    endRel.x.toFloat(),
    endRel.y.toFloat()
  )
  return path
}

private val topBottomCache = mutableMapOf<SlurArgs, Pair<Int, Int>>()

private val canvas = Canvas()

private fun getVisibleTopBottom(
  slurArgs: SlurArgs,
  width: Int,
  height: Int,
  shapeDrawable: Drawable,
  up: Boolean
): Pair<Int, Int> {

  return topBottomCache[slurArgs] ?: run {

    if (width > 0 && height > 0) {

      val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)

      canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
      canvas.setBitmap(bitmap)
      shapeDrawable.draw(canvas)

      val mid = bitmap.width / 2
      val range = if (up) (0 until bitmap.height) else (bitmap.height - 1) downTo 0

      range.forEach { y ->
        if (bitmap.getPixel(mid, y) != 0) {
          val res = if (up) Pair(y, 0) else Pair(0, height - y)
          topBottomCache[slurArgs] = res
          return res
        }
      }
    }
    Pair(0, 0)
  }
}