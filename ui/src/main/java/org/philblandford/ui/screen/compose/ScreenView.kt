package org.philblandford.ui.screen.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.philblandford.ascore.android.ui.style.lightGray
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ascore2.features.scorelayout.usecases.ScoreLayout
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.screen.viewmodels.ScreenInterface
import org.philblandford.ui.screen.viewmodels.ScreenViewModel
import org.philblandford.ui.util.ZoomView
import timber.log.Timber

@Composable
fun ScreenView() {

  VMView(ScreenViewModel::class.java) { model, iface, _ ->

    BoxWithConstraints(
      Modifier
        .fillMaxSize()
        .background(lightGray)
    ) {
      val defaultScale = calculateDefaultScale(model.scoreLayout)
      val canvasSize = calculateCanvasSize(model.scoreLayout)

      ScreenPages(
        model.scoreLayout.numPages,
        defaultScale,
        canvasSize.first,
        canvasSize.second,
        model.updateCounter,
        iface)


    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScreenPages(
  num: Int, drawingScale: Float,
  canvasWidth: Dp, canvasHeight: Dp,
  redraw: Int,
  iface: ScreenInterface,
) {

  ZoomView { modifier, onDoubleTap ->
    LazyColumn(
      modifier.fillMaxWidth()
    ) {
      items(num) { page ->
        ScreenPage(
          Modifier.size(canvasWidth, canvasHeight),
          page + 1,
          redraw,
          iface,
          drawingScale,
          onTap = {
            Timber.e("onTap $it")
            iface.handleTap(page+1, (it.x).toInt(), (it.y).toInt())
          },
          onLongPress = {
            iface.handleLongPress(page + 1, (it.x).toInt(), (it.y).toInt())
          },
          onDoubleTap = onDoubleTap,
          onDrag = {
            iface.handleDrag(it.x, it.y)
          }
        )
      }
    }
  }
}

@Composable
private fun ScreenPage(
  modifier: Modifier, num: Int,
  redraw: Int,
  iface: ScreenInterface,
  drawingScale: Float,
  onTap:(Offset)->Unit = {},
  onLongPress:(Offset) -> Unit = {},
  onDoubleTap:()->Unit = {},
  onDrag:(Offset) ->Unit = {}
) {
  Canvas(
    modifier
      .padding(5.dp)
      .background(Color.White)
      .pointerInput(Unit) {


        detectTapGestures(
          onTap = { offset ->
            onTap(Offset(offset.x / drawingScale, offset.y / drawingScale))

          },
          onLongPress = { offset ->
            onLongPress(Offset(offset.x / drawingScale, offset.y / drawingScale))
          },
          onDoubleTap = { onDoubleTap() }

        )
      }.pointerInput(Unit) {
        detectDragGesturesAfterLongPress { _, dragAmount ->
          onDrag(dragAmount)
        }
      }
  ) {
    scale(drawingScale, pivot = Offset(0f, 0f)) {
      redraw.let {
        iface.drawPage(num, this)
      }
    }
  }
}

@Composable
private fun BoxWithConstraintsScope.calculateDefaultScale(scoreLayout: ScoreLayout): Float {
  val density = LocalDensity.current.density
  val margin = 5.dp
  val rawScreenWidth = (maxWidth - (margin * 2)) * density
  return (rawScreenWidth / scoreLayout.width).value
}

@Composable
private fun BoxWithConstraintsScope.calculateCanvasSize(scoreLayout: ScoreLayout): Pair<Dp, Dp> {
  val ratio = scoreLayout.height.toFloat() / scoreLayout.width
  return maxWidth to (maxWidth * ratio)
}