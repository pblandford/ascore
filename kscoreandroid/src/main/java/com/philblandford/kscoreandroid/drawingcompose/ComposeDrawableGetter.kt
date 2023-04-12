package com.philblandford.kscoreandroid.drawingcompose

import Preferences
import ResourceManager
import TextFontManager
import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.philblandford.kscore.api.DrawableGetter
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.KDrawable
import com.philblandford.kscore.engine.core.area.factory.*


internal fun Coord.toOffset(): Offset {
  return Offset(x.toFloat(), y.toFloat())
}

abstract class ComposeDrawable(
  protected open val getDrawScope: () -> DrawScope?,
  override val width: Int, override val height: Int,
  override val effectiveHeight: Int = height,
  override val trim: Int = 0,
  override val export: Boolean = true
) : KDrawable {

  override fun draw(x:Int, y:Int, export: Boolean, vararg args:Any) {
    if (export && !this.export) {
      return
    }
    getDrawScope()?.draw(x, y)
  }

  abstract fun DrawScope.draw(x:Int, y:Int)
}

class ComposeStubDrawable(override val getDrawScope: () -> DrawScope?) :
  ComposeDrawable(getDrawScope, 20, 20) {
  override fun DrawScope.draw(x:Int, y:Int) {
    getDrawScope()?.drawCircle(Color.Blue, 20f, Offset(x.toFloat(), y.toFloat()))
  }
}

class ComposeDrawableGetter(
  private val context: Context,
  private val textFontManager: TextFontManager
) : DrawableGetter {

  private var drawScope: DrawScope? = null
  var color:Color = Color.Black

  override fun prepare(vararg args: Any) {
    val ds = args.get(0)
    drawScope = ds as DrawScope
  }

  override fun getDrawable(drawableArgs: DrawableArgs): KDrawable? {
    return when (drawableArgs) {
      is BeamArgs -> composeBeamDrawable(drawableArgs, color) { drawScope }
      is DiagonalArgs -> composeDiagonalDrawable(drawableArgs, color) { drawScope }
      is DotArgs -> composeDotDrawable(drawableArgs, color) { drawScope }
      is ImageArgs -> composeImageDrawable(drawableArgs, context, color) { drawScope }
      is LineArgs -> composeLineDrawable(drawableArgs, color) { drawScope }
      is RectArgs -> composeRectDrawable(drawableArgs) { drawScope }
      is SlurArgs -> composeSlurDrawable(drawableArgs, color) { drawScope }
      is TextArgs -> composeTextDrawable(drawableArgs, textFontManager, color) { drawScope }
      else -> ComposeStubDrawable { drawScope }
    }
  }

}