package com.philblandford.kscoreandroid.drawingandroid

import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import com.philblandford.kscore.engine.core.area.factory.BeamArgs
import com.philblandford.kscore.engine.core.representation.BEAM_THICKNESS


class AndroidBeamDrawable(
  override val drawable: Drawable,
  override val width: Int, override val height: Int
) : AndroidDrawable(width, height) {
}

fun androidBeamDrawable(beamArgs: BeamArgs): AndroidDrawable {
  val drawable = getBeamDrawable(beamArgs.up, beamArgs.width, beamArgs.height)
  return AndroidBeamDrawable(drawable, drawable.bounds.width(), drawable.bounds.height())
}

private data class FloatCoord(val x:Float, val y:Float)

private fun getBeamDrawable(up:Boolean, width: Int, height: Int): ShapeDrawable {
  val startPosTop = FloatCoord(0f, if (!up) 0f else height.toFloat())
  val startPosBottom = FloatCoord(0f, startPosTop.y + BEAM_THICKNESS)
  val endPosTop = FloatCoord(width.toFloat(), if (up) 0f else height.toFloat())
  val endPosBottom = FloatCoord(width.toFloat(), endPosTop.y + BEAM_THICKNESS)
  val path = Path()
  path.moveTo(startPosTop.x, startPosTop.y)
  path.lineTo(endPosTop.x, endPosTop.y)
  path.lineTo(endPosBottom.x, endPosBottom.y)
  path.lineTo(startPosBottom.x, startPosBottom.y)
  path.lineTo(startPosTop.x, startPosTop.y)
  val heightWithThickness = height + BEAM_THICKNESS
  val drawable: ShapeDrawable = ShapeDrawable(PathShape(path, width.toFloat(), heightWithThickness.toFloat()))
  drawable.setBounds(0, 0, width, heightWithThickness)
  setPaint(drawable)
  return drawable
}

private fun setPaint(shapeDrawable: ShapeDrawable) {
  val paint: Paint = shapeDrawable.paint
  paint.style = Paint.Style.FILL
  paint.isAntiAlias = true
}
