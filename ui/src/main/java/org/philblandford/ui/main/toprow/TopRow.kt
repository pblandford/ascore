package org.philblandford.ui.main.toprow

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap
import org.philblandford.ui.common.block
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.compact
import org.philblandford.ui.util.SquareButton

@Composable
fun TopRow(
  modifier: Modifier,
  canShowTabs: Boolean,
  vertical:Boolean,
  openDrawer: () -> Unit,
  showLayoutOptions: () -> Unit,
  fullScreen: () -> Unit,
  showConsole: () -> Unit,
  toggleVertical: () -> Unit
) {
  if (LocalWindowSizeClass.current.compact()) {
    TopRowCompact(
      modifier,
      canShowTabs,
      vertical,
      openDrawer,
      showLayoutOptions,
      fullScreen,
      showConsole,
      toggleVertical
    )
  } else {
    TopRowExpanded(
      modifier,
      vertical,
      openDrawer,
      showLayoutOptions,
      fullScreen,
      showConsole,
      toggleVertical
    )
  }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TopRowCompact(
  modifier: Modifier,
  canShowTabs: Boolean,
  vertical: Boolean,
  openDrawer: () -> Unit,
  showLayoutOptions: () -> Unit,
  fullScreen: () -> Unit,
  showConsole: () -> Unit,
  toggleVertical: () -> Unit
) {
  var showTabs by remember { mutableStateOf(false) }

  Row(
    modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .border(1.dp, MaterialTheme.colorScheme.onSurface)
  ) {
    SquareButton(R.drawable.settings) { openDrawer() }
    if (canShowTabs) {
      SquareButton(R.drawable.tabs) {
        showTabs = !showTabs
      }
    }

    AnimatedContent(showTabs, Modifier.weight(1f),
      transitionSpec =
      {

        (slideInHorizontally { width -> width } with
            slideOutHorizontally { width -> width }).using(
          SizeTransform(clip = false)
        )

      }
    ) {
      if (it) {
        Tabs(Modifier)
      } else {
        Row {
          Box(Modifier.weight(1f))
          SquareButton(R.drawable.fullscreen) {
            fullScreen()
          }
          SquareButton(R.drawable.page) {
            showLayoutOptions()
          }
          SquareButton(if (!vertical) R.drawable.page_down else  R.drawable.page_forward) { toggleVertical() }
          SquareButton(R.drawable.mixer) {
            showConsole()
          }
          PlayButton()
        }
      }

    }

  }
}

@Composable
fun TopRowExpanded(
  modifier: Modifier,
  vertical: Boolean,
  openDrawer: () -> Unit,
  showLayoutOptions: () -> Unit,
  fullScreen: () -> Unit,
  showConsole: () -> Unit,
  toggleVertical: () -> Unit
) {

  Row(
    modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .border(1.dp, MaterialTheme.colorScheme.onSurface)
  ) {
    SquareButton(R.drawable.settings) { openDrawer() }
    Line()
    Tabs(Modifier.weight(1f))
      SquareButton(R.drawable.fullscreen) {
        fullScreen()
      }
      SquareButton(R.drawable.page) {
        showLayoutOptions()
      }
    SquareButton(if (!vertical) R.drawable.page_down else  R.drawable.page_forward) { toggleVertical() }

    SquareButton(R.drawable.mixer) {
        showConsole()
      }
      PlayButton()
  }

}


@Composable
private fun Line() {
  Box(
    Modifier
      .size(1.dp, block())
      .background(MaterialTheme.colorScheme.onSurface)
  )
}