package org.philblandford.ui.screen.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun ScreenViewHorizontal(
  model: ScreenModel,
  iface: ScreenInterface,
  modifier: Modifier,
  effects: Flow<ScreenEffect>,
  scale: MutableState<Float>,
  defaultScale: Float,
  viewPortWidth: Dp,
  page:MutableState<Int>,
  onScoreEmpty: () -> Unit,
  changeMethod: () -> Unit
) {
  var turningForward by remember { mutableStateOf(true) }
  val coroutineScope = rememberCoroutineScope()
  val pageRatio = model.scoreLayout.height.toFloat() / model.scoreLayout.width

  LaunchedEffect(Unit) {
    coroutineScope.launch {
      effects.collectLatest { effect ->
        when (effect) {
          is ScreenEffect.ScrollToPage -> page.value = effect.page
          ScreenEffect.NoScore -> onScoreEmpty()
          else -> {}
        }
      }
    }
  }

  BoxWithConstraints(
    Modifier
      .fillMaxSize()
      .background(veryLightGray)
  ) {

    val width = maxWidth * (scale.value / defaultScale)
    val height = width * pageRatio

    if (page.value <= model.scoreLayout.numPages) {

      AnimatedContent(page.value,
        transitionSpec =
        {
          if (turningForward) {
            (slideInHorizontally { width -> width } with
                slideOutHorizontally { width -> -width }).using(
              SizeTransform(clip = false)
            )

          } else {
            (slideInHorizontally { width -> -width } with
                slideOutHorizontally { width -> width }).using(
              SizeTransform(clip = false)
            )
          }
        }, modifier = modifier
      ) {

        key(defaultScale) {

          ScreenPage(
            page.value, model.updateCounter, model.editItem, iface,
            {
              scale.value
            },
            defaultScale, 1.5f, width, height, viewPortWidth,
            { scale.value = it },
            { offset -> iface.handleTap(page.value, offset.x.toInt(), offset.y.toInt()) },
            { offset -> iface.handleLongPress(page.value, offset.x.toInt(), offset.y.toInt()) },
            {
              scale.value = defaultScale;
            },
            { offset -> iface.handleDrag(offset.x, offset.y) },
            iface::handleLongPressRelease,
            turnPage = { left ->
              page.value = if (left) {
                (page.value - 1).coerceAtLeast(1)
              } else {
                (page.value + 1).coerceAtMost(model.scoreLayout.numPages)
              }
              turningForward = !left
              Timber.e("Turned to $page")
            }, vertical = false,
            changeMethod = changeMethod
          )
        }
      }
    } else {
      Box(Modifier.size(width, height))
    }
  }
}


