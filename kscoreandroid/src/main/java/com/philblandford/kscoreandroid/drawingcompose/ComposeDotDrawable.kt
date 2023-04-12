package com.philblandford.kscoreandroid.drawingcompose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DotArgs

class ComposeDotDrawable(
  override val width: Int,
  override val height: Int,
  private val color:Color = Color.Black,
  override val getDrawScope: () -> DrawScope?
) : ComposeDrawable(getDrawScope, width, height) {

  override fun DrawScope.draw(x:Int, y:Int) {
    val centre = Coord(x + width/2, y + height/2)
    drawCircle(color, width.toFloat()/2, centre.toOffset())
  }
}

fun composeDotDrawable(dotArgs: DotArgs,
                       color: Color = Color.Black,
                       getDrawScope: () -> DrawScope?): ComposeDrawable {
  return ComposeDotDrawable(dotArgs.width, dotArgs.height, color, getDrawScope)
}