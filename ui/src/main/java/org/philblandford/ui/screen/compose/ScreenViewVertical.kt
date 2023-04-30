package org.philblandford.ui.screen.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.philblandford.ui.theme.compose.veryLightGray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ui.screen.viewmodels.ScreenEffect
import org.philblandford.ui.screen.viewmodels.ScreenInterface
import org.philblandford.ui.screen.viewmodels.ScreenModel
import timber.log.Timber

@Composable
internal fun ScreenViewVertical(
  model: ScreenModel,
  iface: ScreenInterface,
  effects: Flow<ScreenEffect>,
  scale: MutableState<Float>,
  defaultScale: Float,
  viewPortWidth: Dp,
  onScoreEmpty: () -> Unit,
  changeMethod:()->Unit
) {
  BoxWithConstraints(Modifier.fillMaxSize()) {
    val pageRatio = model.scoreLayout.height.toFloat() / model.scoreLayout.width

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val scrollX = rememberScrollState()

    LaunchedEffect(Unit) {
      coroutineScope.launch {
        effects.collectLatest { effect ->
          when (effect) {
            is ScreenEffect.ScrollToPage -> lazyListState.scrollToItem(effect.page - 1)
            ScreenEffect.NoScore -> {
              Timber.e("NS effect received")
              onScoreEmpty()
            }

            else -> {}
          }
        }
      }
    }

    LaunchedEffect(Unit) {
      coroutineScope.launch {
        effects.collectLatest { effect ->
          when (effect) {
            is ScreenEffect.ScrollToPage -> lazyListState.scrollToItem(effect.page - 1)
            ScreenEffect.NoScore -> {
              Timber.e("NS effect received")
              onScoreEmpty()
            }

            else -> {}
          }
        }
      }
    }



    key(defaultScale) {
      LazyColumn(Modifier.background(veryLightGray), state = lazyListState) {

        items(model.scoreLayout.numPages + 1) { idx ->

          val width = maxWidth * (scale.value / defaultScale)
          val height = width * pageRatio
          val page = idx + 1

          if (idx < model.scoreLayout.numPages) {
            ScreenPage(
              page, model.updateCounter, model.editItem, iface,
              {
                scale.value
              },
              defaultScale, 1.5f, width, height, viewPortWidth,
              { scale.value = it },
              { offset -> iface.handleTap(page, offset.x.toInt(), offset.y.toInt()) },
              { offset -> iface.handleLongPress(page, offset.x.toInt(), offset.y.toInt()) },
              {
                scale.value = defaultScale;
                coroutineScope.launch { lazyListState.scrollToItem(idx) }
              },
              { offset -> iface.handleDrag(offset.x, offset.y) },
              iface::handleLongPressRelease,
              onVerticalScroll = {
                coroutineScope.launch {
                  lazyListState.scrollBy(it)
                }
              },
              lazyListState = lazyListState,
              changeMethod = changeMethod,
              scrollX = scrollX
            )
          } else {
            Box(Modifier.size(width, height))
          }
        }
      }

    }
  }
}


