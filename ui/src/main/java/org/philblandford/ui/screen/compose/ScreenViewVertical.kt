package org.philblandford.ui.screen.compose

import android.icu.number.Scale
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
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
import com.philblandford.ascore.android.ui.style.veryLightGray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.scorelayout.usecases.ScoreLayout
import org.philblandford.ascore2.features.ui.model.EditItem
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.edit.compose.EditPanel
import org.philblandford.ui.screen.viewmodels.ScreenEffect
import org.philblandford.ui.screen.viewmodels.ScreenInterface
import org.philblandford.ui.screen.viewmodels.ScreenModel
import org.philblandford.ui.screen.viewmodels.ScreenViewModel
import timber.log.Timber
import kotlin.math.abs

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
      val currentIsTop = remember { derivedStateOf {  } }

      LazyColumn(Modifier.background(veryLightGray), state = lazyListState) {

        items(model.scoreLayout.numPages + 1) { idx ->

          Timber.e("currentIsTop ${currentIsTop.value}")

          val width = maxWidth * (scale.value / defaultScale)
          val height = width * pageRatio
          val page = idx + 1

          if (idx < model.scoreLayout.numPages) {
            ScreenPage(
              page, model.updateCounter, model.editItem, iface,
              {
                Timber.e("detect getScale $scale $defaultScale")
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


