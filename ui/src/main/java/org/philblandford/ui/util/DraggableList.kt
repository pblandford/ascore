/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.philblandford.ui.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : Any> DraggableList(
  items: List<T>,
  itemContent: @Composable (T) -> Unit,
  reorder: (Int, Int) -> Unit,
  vertical:Boolean = true,
  modifier: Modifier = Modifier,
  listState: LazyListState = rememberLazyListState(),
  key:(Int, T)->Any = { _, item -> item},

  ) {

  val dragDropState = rememberDragDropState(listState, vertical) { fromIndex, toIndex ->
    reorder(fromIndex, toIndex)
  }

  if (vertical) {
    LazyColumn(
      modifier = modifier.dragContainer(dragDropState),
      state = listState,
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      itemsIndexed(items, key = key) { index, item ->
        DraggableItem(dragDropState, index) { isDragging ->
          val elevation by animateDpAsState(if (isDragging) 4.dp else 1.dp)
          Card(elevation = cardElevation(defaultElevation = elevation)) {
            itemContent(item)
          }
        }
      }
    }
  } else {
    LazyRow(
      modifier = modifier.dragContainer(dragDropState),
      state = listState,
      contentPadding = PaddingValues(16.dp),
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      itemsIndexed(items, key = key) { index, item ->
        DraggableItem(dragDropState, index, false) { isDragging ->
          val elevation by animateDpAsState(if (isDragging) 4.dp else 1.dp)
          Card(elevation = cardElevation(defaultElevation = elevation)) {
            itemContent(item)
          }
        }
      }
    }
  }
}

@Composable
fun rememberDragDropState(
  lazyListState: LazyListState,
  vertical: Boolean,
  onMove: (Int, Int) -> Unit
): DragDropState {
  val scope = rememberCoroutineScope()
  val state = remember(lazyListState) {
    DragDropState(
      state = lazyListState,
      onMove = onMove,
      scope = scope,
      vertical = vertical
    )
  }
  LaunchedEffect(state) {
    while (true) {
      val diff = state.scrollChannel.receive()
      lazyListState.scrollBy(diff)
    }
  }
  return state
}

class DragDropState internal constructor(
  private val state: LazyListState,
  private val scope: CoroutineScope,
  private val vertical: Boolean,
  private val onMove: (Int, Int) -> Unit
) {
  var draggingItemIndex by mutableStateOf<Int?>(null)
    private set

  internal val scrollChannel = Channel<Float>()

  private var draggingItemDraggedDelta by mutableStateOf(0f)
  private var draggingItemInitialOffset by mutableStateOf(0)
  internal val draggingItemOffset: Float
    get() = draggingItemLayoutInfo?.let { item ->
      draggingItemInitialOffset + draggingItemDraggedDelta - item.offset
    } ?: 0f

  private val draggingItemLayoutInfo: LazyListItemInfo?
    get() = state.layoutInfo.visibleItemsInfo
      .firstOrNull { it.index == draggingItemIndex }

  internal var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
    private set
  internal var previousItemOffset = Animatable(0f)
    private set

  internal fun onDragStart(offset: Offset) {
    state.layoutInfo.visibleItemsInfo
      .firstOrNull { item ->
        if (vertical ) offset.y.toInt() in item.offset..(item.offset + item.size)
        else offset.x.toInt() in item.offset..(item.offset + item.size)
      }?.also {
        draggingItemIndex = it.index
        draggingItemInitialOffset = it.offset
      }
  }

  internal fun onDragInterrupted() {
    if (draggingItemIndex != null) {
      previousIndexOfDraggedItem = draggingItemIndex
      val startOffset = draggingItemOffset
      scope.launch {
        previousItemOffset.snapTo(startOffset)
        previousItemOffset.animateTo(
          0f,
          spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = 1f
          )
        )
        previousIndexOfDraggedItem = null
      }
    }
    draggingItemDraggedDelta = 0f
    draggingItemIndex = null
    draggingItemInitialOffset = 0
  }

  internal fun onDrag(offset: Offset) {
    if (vertical) draggingItemDraggedDelta += offset.y
    else draggingItemDraggedDelta += offset.x

    val draggingItem = draggingItemLayoutInfo ?: return
    val startOffset = draggingItem.offset + draggingItemOffset
    val endOffset = startOffset + draggingItem.size
    val middleOffset = startOffset + (endOffset - startOffset) / 2f

    val targetItem = state.layoutInfo.visibleItemsInfo.find { item ->
      middleOffset.toInt() in item.offset..item.offsetEnd &&
              draggingItem.index != item.index
    }
    if (targetItem != null) {
      val scrollToIndex = if (targetItem.index == state.firstVisibleItemIndex) {
        draggingItem.index
      } else if (draggingItem.index == state.firstVisibleItemIndex) {
        targetItem.index
      } else {
        null
      }
      if (scrollToIndex != null) {
        scope.launch {
          // this is needed to neutralize automatic keeping the first item first.
          state.scrollToItem(scrollToIndex, state.firstVisibleItemScrollOffset)
          onMove.invoke(draggingItem.index, targetItem.index)
        }
      } else {
        onMove.invoke(draggingItem.index, targetItem.index)
      }
      draggingItemIndex = targetItem.index
    } else {
      val overscroll = when {
        draggingItemDraggedDelta > 0 ->
          (endOffset - state.layoutInfo.viewportEndOffset).coerceAtLeast(0f)
        draggingItemDraggedDelta < 0 ->
          (startOffset - state.layoutInfo.viewportStartOffset).coerceAtMost(0f)
        else -> 0f
      }
      if (overscroll != 0f) {
        scrollChannel.trySend(overscroll)
      }
    }
  }

  private val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size
}

fun Modifier.dragContainer(dragDropState: DragDropState): Modifier {
  return pointerInput(dragDropState) {
    detectDragGesturesAfterLongPress(
      onDrag = { change, offset ->
        change.consume()
        dragDropState.onDrag(offset = offset)
      },
      onDragStart = { offset -> dragDropState.onDragStart(offset) },
      onDragEnd = { dragDropState.onDragInterrupted() },
      onDragCancel = { dragDropState.onDragInterrupted() }
    )
  }
}

@ExperimentalFoundationApi
@Composable
fun LazyItemScope.DraggableItem(
  dragDropState: DragDropState,
  index: Int,
  vertical: Boolean = true,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.(isDragging: Boolean) -> Unit
) {
  val dragging = index == dragDropState.draggingItemIndex
  val draggingModifier = if (dragging) {
    Modifier
      .zIndex(1f)
      .graphicsLayer {
        if (vertical) translationY = dragDropState.draggingItemOffset
        else translationX = dragDropState.draggingItemOffset
      }
  } else if (index == dragDropState.previousIndexOfDraggedItem) {
    Modifier
      .zIndex(1f)
      .graphicsLayer {
        if (vertical) translationY = dragDropState.previousItemOffset.value
        else translationX = dragDropState.previousItemOffset.value
      }
  } else {
    Modifier.animateItemPlacement()
  }
  Column(modifier = modifier.then(draggingModifier)) {
    content(dragging)
  }
}

@Composable
@Preview
private fun Preview() {
  var instruments by remember { mutableStateOf(List(50) { it }) }
  DraggableList(instruments, { item ->
    Text("Instrument $item", Modifier.padding(10.dp))
  },
    { old, new -> instruments = instruments.reorder(old, new) }
  )
}