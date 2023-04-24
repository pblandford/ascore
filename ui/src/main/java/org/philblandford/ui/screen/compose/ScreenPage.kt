package org.philblandford.ui.screen.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.ui.model.EditItem
import org.philblandford.ui.R
import org.philblandford.ui.edit.compose.EditPanel
import org.philblandford.ui.screen.viewmodels.ScreenInterface
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.absoluteValue

@Composable
internal fun ScreenPage(
  num: Int,
  redraw: Int,
  editItem: EditItem?,
  iface: ScreenInterface,
  getScale: () -> Float,
  minScale: Float,
  maxScale: Float,
  width: Dp,
  height: Dp,
  viewPortWidth: Dp,
  setScale: (Float) -> Unit,
  onTap: (Offset) -> Unit = {},
  onLongPress: (Offset) -> Unit = {},
  onDoubleTap: () -> Unit = {},
  onDrag: (Offset) -> Unit = {},
  onLongPressRelease: () -> Unit = {},
  onVerticalScroll: (Float) -> Unit = {},
  turnPage: (Boolean) -> Unit = {},
  changeMethod:()->Unit,
  vertical:Boolean = true,
  lazyListState: LazyListState = rememberLazyListState(),
  scrollX:ScrollState = rememberScrollState()
  ) {
  Timber.e("RECO ScreenPage $num")

  Timber.e("detect recompose ${getScale()}")

  val density = LocalDensity.current.density
  val coroutineScope = rememberCoroutineScope()
  val scrollY = rememberScrollState()

  Timber.e("detect scroll ${scrollX.value} ${scrollY.value}")

  BoxWithConstraints(
    Modifier
      .fillMaxWidth()
      .height(height)
      .padding(5.dp, 0.dp, 5.dp, 10.dp)
      .horizontalScroll(scrollX)
      .verticalScroll(scrollY, !vertical)
  ) {
    val remHeight = height

    Surface(Modifier.fillMaxSize(), color = Color.White, tonalElevation = 10.dp, shadowElevation = 5.dp) {

      Image(
        painterResource(R.drawable.paper), "",
        Modifier
          .size(width - 10.dp, height)
          .graphicsLayer { alpha = 0.3f },
        contentScale = ContentScale.FillBounds,
      )

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
              onDoubleTap = {
                onDoubleTap()
                coroutineScope.launch {
                  scrollX.scrollTo(0)
                }
              },
              onPress = {
                awaitRelease()
                onLongPressRelease()
              }
            )
          }
          .pointerInput(Unit) {
            detectDragGesturesAfterLongPress { _, dragAmount ->
              onDrag(dragAmount)
            }
          }
          .pointerInput(getScale()) {
            if (getScale() == minScale) {
              var totalDrag = 0f
              detectHorizontalDragGestures(
                onDragStart = { totalDrag = 0f },
                onDragEnd = {
                  if (totalDrag.absoluteValue > 80f) {
                    if (vertical) {
                      changeMethod()
                    } else {
                      turnPage(totalDrag > 0)
                    }
                  }
                },
                onHorizontalDrag = { change, dragAmount ->
                  if (dragAmount.absoluteValue > 80) {
                    totalDrag += dragAmount
                  }
                }
              )
            }
          }
          .pointerInput(Unit) {
            if (!vertical) {
              detectVerticalDragGestures { change, dragAmount ->
                if (getScale() == minScale && dragAmount.absoluteValue > 80) {
                  changeMethod()
                } else {
                  coroutineScope.launch {
                    scrollY.scrollBy(-dragAmount)
                  }
                }
              }
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
              if (vertical) {
                val verticalScrollAmount =
                  if (lazyListState.firstVisibleItemIndex == num - 1) {
                    -offsetAddition.y * density
                  } else {
                    val realHeight = remHeight * density * (newValue / minScale)
                    -offsetAddition.y * density * (realHeight.value / centroid.y)
                  }
                Timber.e("current $verticalScrollAmount")
                onVerticalScroll(verticalScrollAmount)
              } else {
                val verticalScrollTarget = scrollY.value - offsetAddition.y * density
                coroutineScope.launch {
                  scrollY.scrollTo(verticalScrollTarget.toInt())
                }
              }
              coroutineScope.launch {
                scrollX.scrollTo(horizontalScrollTarget.toInt())
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
    EditOverlay(editItem, scrollX.value, num, viewPortWidth, getScale)
  }

}

@Composable
private fun BoxScope.EditOverlay(
  editItem: EditItem?, scrollX: Int, page: Int, viewPortWidth: Dp,
  getScale: () -> Float
) {
  val density = LocalDensity.current.density

  editItem?.let {
    key(scrollX) {
      if (it.page == page) {

        Timber.e("MOVEITEM $editItem")

        val editSize = remember { mutableStateOf(IntSize(0, 0)) }

        Timber.e("SV editSize $editSize")
        val rightEdge = (scrollX / density) + viewPortWidth.value - 10
        Timber.e("SV right edge $rightEdge")

        val offset = Offset(
          (editItem.rectangle.x.toFloat() * getScale() / density)
            .coerceAtMost(rightEdge - editSize.value.width),
          (editItem.rectangle.y + editItem.rectangle.height + 150).toFloat() * getScale() / density
        )
        Timber.e("SV offset $offset $viewPortWidth")
        EditPanel(
          Modifier
            .align(Alignment.TopStart)
            .offset(offset.x.dp, offset.y.dp)
            .onGloballyPositioned { editSize.value = it.size.div(density.toInt()) },
          editItem.event.eventType,
          getScale()
        )
      }
    }
  }
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

