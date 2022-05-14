package org.philblandford.ui.screen.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.philblandford.ascore.android.ui.style.lightGray
import com.philblandford.kscore.log.ksLogt
import kotlinx.coroutines.flow.collect
import org.philblandford.ascore2.features.scorelayout.usecases.ScoreLayout
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.screen.viewmodels.ScreenEffect
import org.philblandford.ui.screen.viewmodels.ScreenInterface
import org.philblandford.ui.screen.viewmodels.ScreenViewModel

@Composable
fun ScreenView() {

  VMView(ScreenViewModel::class.java) { model, iface, effects ->

    BoxWithConstraints(Modifier.fillMaxSize().background(lightGray)) {
      val defaultScale = calculateDefaultScale(model.scoreLayout)
      val scale = remember { mutableStateOf(defaultScale) }
      scale.value = calculateDefaultScale(model.scoreLayout)

      val canvasSize = calculateCanvasSize(model.scoreLayout)

      model.updateCounter.let { counter ->
        ScreenPages(
          model.scoreLayout.numPages,
          scale.value,
          canvasSize.first,
          canvasSize.second,
          counter,
          iface
        )
      }
    }
  }
}

@Composable
private fun ScreenPages(
  num: Int, scale: Float,
  canvasWidth: Dp, canvasHeight: Dp,
  redraw: Int,
  iface: ScreenInterface
) {

    LazyColumn {
      items(num) { page ->
        ScreenPage(
          Modifier.size(canvasWidth, canvasHeight),
          page + 1,
          scale,
          redraw,
          iface
        )
      }
  }
}

@Composable
private fun ScreenPage(
  modifier: Modifier, num: Int, scale: Float,
  redraw: Int,
  iface:ScreenInterface
) {
  Canvas(
    modifier.padding(5.dp)
      .background(Color.White).tap(num, scale, iface)
  ) {
    scale(scale, pivot = Offset(0f, 0f)) {
      redraw.let {
        iface.drawPage(num, this)
      }
    }
  }
}

private fun Modifier.tap(
  pageNum:Int,
  scale: Float,
  iface:ScreenInterface) = this.then(pointerInput(Unit) {
    detectTapGestures(
      onTap = {
        ksLogt("tap $it $scale")
        iface.handleTap(pageNum, (it.x / scale).toInt(), (it.y / scale).toInt())
      },
      onLongPress = {
        iface.handleLongPress(pageNum, (it.x / scale).toInt(), (it.y / scale).toInt())
      },
      onDoubleTap = {
      }
    )
  })


@Composable
private fun BoxWithConstraintsScope.calculateDefaultScale(scoreLayout: ScoreLayout): Float {
  val density = LocalDensity.current.density
  val margin = 5.dp
  val rawScreenWidth = (maxWidth - (margin * 2)) * density
  return (rawScreenWidth / scoreLayout.width).value
}

@Composable
private fun BoxWithConstraintsScope.calculateCanvasSize(scoreLayout: ScoreLayout):Pair<Dp, Dp> {
  val ratio = scoreLayout.height.toFloat() / scoreLayout.width
  return maxWidth to (maxWidth * ratio)
}