package org.philblandford.ui.util

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import org.philblandford.ui.theme.AscoreTheme
import timber.log.Timber

@ExperimentalFoundationApi
@Composable
fun ZoomView(
  modifier: Modifier = Modifier,
  maxScale: Float = 10f,
  minScale: Float = 0.1f,
  startScale: Float = 1f,
  scrollState: ScrollableState? = null,
  contents: @Composable (Modifier, ()->Unit) -> Unit
) {
  val coroutineScope = rememberCoroutineScope()


  var scale by remember { mutableStateOf(startScale) }
  var offsetX by remember { mutableStateOf(1f) }
  var offsetY by remember { mutableStateOf(1f) }

  val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
    Timber.e("rts $zoomChange $panChange")
    scale *= zoomChange
    scrollState?.run {
      coroutineScope.launch {
        setScrolling(false)
      }
    }
    offsetX += panChange.x
    offsetY += panChange.y
    scrollState?.run {
      coroutineScope.launch {
        setScrolling(true)
      }
    }
  }

  Box(
    modifier = Modifier
      .pointerInput(Unit) {
        detectTapGestures(
          onDoubleTap = {
            Timber.e("double tap $it")
            scale = startScale
            offsetX = 1f
            offsetY = 1f
          }
        )
      }
      .transformable(transformableState)
  )

  {
    contents(
      modifier
        .graphicsLayer {
          scaleX = scale.coerceIn(minScale, maxScale)
          scaleY = scale.coerceIn(minScale, maxScale)
          translationX = offsetX
          translationY = offsetY
        }) {
      Timber.e("double tap")
      scale = startScale
      offsetX = 1f
      offsetY = 1f

    }
  }
}

suspend fun ScrollableState.setScrolling(value: Boolean) {
  scroll(scrollPriority = MutatePriority.PreventUserInput) {
    when (value) {
      true -> Unit
      else -> awaitCancellation()
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
private fun Preview() {
  AscoreTheme {
    ZoomView { modifier, _ ->
      LazyColumn(modifier.fillMaxSize()) {
        items((0..50).toList()) { item ->
          Card(
            Modifier
              .fillMaxWidth(0.9f)
              .height(300.dp)
              .background(Color(0xffaabbff)),
            elevation = 4.dp
          ) {
            Box(Modifier.fillMaxSize()) {
              Text("Box No $item", Modifier.align(Alignment.Center))
            }
          }
        }
      }
    }
  }
}
