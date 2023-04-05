package org.philblandford.ui.util

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import timber.log.Timber

@Composable
fun DraggableItem(modifier: Modifier, offset: MutableState<Offset>,
                  content: @Composable ()->Unit) {

  val density = LocalDensity.current.density

  Box(modifier.offset(offset.value.x.dp, offset.value.y.dp).pointerInput(Unit) {
    detectDragGestures { _, dragAmount ->
      Timber.e("draggableItem $dragAmount")
      offset.value += dragAmount / density
    }
  }) {
    content()
  }
}