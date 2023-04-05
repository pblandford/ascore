package org.philblandford.ui.screen.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.philblandford.ascore.android.ui.style.lightGray
import com.philblandford.ascore.android.ui.style.veryLightGray
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.scorelayout.usecases.ScoreLayout
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.screen.viewmodels.ScreenInterface
import org.philblandford.ui.screen.viewmodels.ScreenModel
import org.philblandford.ui.screen.viewmodels.ScreenViewModel
import timber.log.Timber
import kotlin.math.abs

@Composable
fun ScreenView2() {
  VMView(ScreenViewModel::class.java) { model, iface, _ ->
    ScreenViewInternal(model, iface)
  }
}

@Composable
private fun ScreenViewInternal(model: ScreenModel, iface: ScreenInterface) {
  BoxWithConstraints(Modifier.fillMaxSize()) {
    val defaultScale = calculateDefaultScale(model.scoreLayout)
    val scale = remember(defaultScale) { mutableStateOf(defaultScale) }
    val pageRatio = model.scoreLayout.height.toFloat() / model.scoreLayout.width
    val lazyListState = rememberLazyListState()
    val scrollX = rememberScrollState()

    Timber.e("detect TOP $defaultScale ${scale}")

    key(defaultScale) {
      LazyColumn(Modifier.background(veryLightGray), state = lazyListState) {
        items(model.scoreLayout.numPages + 1) { idx ->
          val width = maxWidth * (scale.value / defaultScale)
          val height = width * pageRatio
          val page = idx + 1

          if (idx < model.scoreLayout.numPages) {
            ScreenPage(page, model.updateCounter, iface,
              {
                Timber.e("detect getScale $scale $defaultScale")
                scale.value
              },
              defaultScale, 20f, width, height, scrollX, lazyListState,
              { scale.value = it },
              { offset -> iface.handleTap(page, offset.x.toInt(), offset.y.toInt()) },
              { offset -> iface.handleLongPress(page, offset.x.toInt(), offset.y.toInt()) },
              { scale.value = defaultScale },
              { offset -> iface.handleDrag(offset.x, offset.y) }
            )
          } else {
            Box(Modifier.size(width, height))
          }
        }
      }

    }
  }
}

@Composable
private fun ScreenPage(
  num: Int,
  redraw: Int,
  iface: ScreenInterface,
  getScale: () -> Float,
  minScale: Float,
  maxScale: Float,
  width: Dp,
  height: Dp,
  scrollX: ScrollState,
  lazyListState: LazyListState,
  setScale: (Float) -> Unit,
  onTap: (Offset) -> Unit = {},
  onLongPress: (Offset) -> Unit = {},
  onDoubleTap: () -> Unit = {},
  onDrag: (Offset) -> Unit = {},
) {
  Timber.e("detect recompose ${getScale()}")

  val density = LocalDensity.current.density
  val coroutineScope = rememberCoroutineScope()

  BoxWithConstraints(
    Modifier
      .fillMaxWidth()
      .height(height)
      .padding(5.dp, 0.dp, 5.dp, 10.dp)
      .horizontalScroll(scrollX)

  ) {
    Surface(Modifier.fillMaxSize(), color = Color.White, elevation = 5.dp) {
      val remHeight = maxHeight

      Canvas(
        Modifier
          .size(width - 10.dp, height)
          .pointerInput(Unit) {
            detectTapGestures(
              onTap = { offset ->
                val scale = getScale()
                onTap(Offset(offset.x / scale, offset.y / scale))
              },
              onLongPress = { offset ->
                val scale = getScale()
                onLongPress(Offset(offset.x / scale, offset.y / scale))
              },
              onDoubleTap = { onDoubleTap() }
            )
          }
          .pointerInput(Unit) {
            detectDragGesturesAfterLongPress { _, dragAmount ->
              onDrag(dragAmount)
            }
          }
          .pointerInput(Unit) {
            detectZoomGestures { centroid, zoom ->
              val scale = getScale()
              val newValue = (scale * zoom).coerceIn(minScale, maxScale)
              Timber.e("detect transform $centroid $zoom $newValue ${scale}")

              if (newValue == scale) return@detectZoomGestures

              val newDistanceFromEdge = centroid * zoom
              val offsetAddition = (centroid - newDistanceFromEdge) / density

              val horizontalScrollTarget = scrollX.value - offsetAddition.x * density
              val verticalScrollAmount = if (lazyListState.firstVisibleItemIndex == num - 1) {
                -offsetAddition.y * density
              } else {
                val realHeight = remHeight * density * (newValue / minScale)
                -offsetAddition.y * density * (realHeight.value / centroid.y)
              }
              coroutineScope.launch {
                scrollX.scrollTo(horizontalScrollTarget.toInt())
                lazyListState.scrollBy(verticalScrollAmount)
              }
              setScale(newValue)
            }
          }
      ) {
        scale(getScale(), pivot = Offset(0f, 0f)) {
          redraw.let {
            iface.drawPage(num, this)
          }
        }
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

suspend fun PointerInputScope.detectZoomGestures(
  onGesture: (centroid: Offset, zoom: Float) -> Unit
) {
  forEachGesture {
    awaitPointerEventScope {
      var zoom = 1f
      var pastTouchSlop = false
      val touchSlop = viewConfiguration.touchSlop

      awaitFirstDown(requireUnconsumed = false)
      var firstCentroid = true
      do {
        val event = awaitPointerEvent()
        val canceled = event.changes.any { it.isConsumed }
        if (!canceled) {
          val zoomChange = event.calculateZoom()

          if (!pastTouchSlop) {
            zoom *= zoomChange

            val centroidSize = event.calculateCentroidSize(useCurrent = false)
            val zoomMotion = abs(1 - zoom) * centroidSize

            if (zoomMotion > touchSlop) {
              pastTouchSlop = true
            }
          }

          if (pastTouchSlop) {
            val centroid = event.calculateCentroid(useCurrent = false)
            if (firstCentroid) {
              Timber.e("CENTROID $centroid")
              firstCentroid = false
            }
            if (zoomChange != 1f) {
              onGesture(centroid, zoomChange)
            }
            event.changes.forEach {
              if (it.positionChanged()) {
                it.consume()
              }
            }
          }
        }
      } while (!canceled && event.changes.any { it.pressed })
    }
  }
}

