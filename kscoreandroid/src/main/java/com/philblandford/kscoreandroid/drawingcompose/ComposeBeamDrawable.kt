package com.philblandford.kscoreandroid.drawingcompose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.philblandford.kscore.engine.core.area.factory.BeamArgs
import com.philblandford.kscore.engine.core.representation.BEAM_THICKNESS


class ComposeBeamDrawable(
  private val up:Boolean,
  override val width: Int,
  override val height: Int,
  private val color: Color = Color.Black,
  override val getDrawScope: () -> DrawScope?
) : ComposeDrawable(getDrawScope, width, height + BEAM_THICKNESS) {

  override fun DrawScope.draw(x:Int, y:Int) {
    val xf = x.toFloat()
    val yf = y.toFloat()

    val startPosTop = FloatCoord(xf, if (!up) yf else yf + height.toFloat())
    val startPosBottom = FloatCoord(xf, startPosTop.y + BEAM_THICKNESS)
    val endPosTop = FloatCoord(xf + width.toFloat(), if (up) yf else yf + height.toFloat())
    val endPosBottom = FloatCoord(xf + width.toFloat(), endPosTop.y + BEAM_THICKNESS)

    val path = Path()
    path.moveTo(startPosTop.x, startPosTop.y)
    path.lineTo(endPosTop.x, endPosTop.y)
    path.lineTo(endPosBottom.x, endPosBottom.y)
    path.lineTo(startPosBottom.x, startPosBottom.y)
    path.lineTo(startPosTop.x, startPosTop.y)
    drawPath(path, color)
  }
}

fun composeBeamDrawable(beamArgs: BeamArgs, color:Color = Color.Black,
                        getDrawScope: () -> DrawScope?): ComposeDrawable {

  return ComposeBeamDrawable(
    beamArgs.up, beamArgs.width, beamArgs.height, color, getDrawScope
  )
}

private data class FloatCoord(val x:Float, val y:Float)
