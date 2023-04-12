package org.philblandford.ui.main.outer.compose

import androidx.compose.material.DrawerValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.main.inputpage.compose.MainPageView
import org.philblandford.ui.main.outer.model.drawerItems
import org.philblandford.ui.main.popup.compose.SettingsDialog
import timber.log.Timber


@Composable
fun OuterPage() {

  Timber.e("RECO OuterPage")

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val popupLayout = remember { mutableStateOf<LayoutID?>(null) }

  ModalDrawer(drawerState = drawerState,
    drawerShape = customShape(),
    drawerBackgroundColor = Color.Transparent,
    drawerContent = {
      DrawerItems(
        drawerItems, { drawerItem ->
          popupLayout.value = drawerItem.layoutID
          scope.launch {
            drawerState.close()
          }
        }) { scope.launch { drawerState.close() } }
    }) {
    MainPageView( { scope.launch { drawerState.open() } }) { popupLayout.value = it }
    popupLayout.value?.let { popup ->
      SettingsDialog(popup) {
        popupLayout.value = null
      }
    }
  }
}

@Composable
fun customShape() = object : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density
  ): Outline {
    return Outline.Rectangle(
      Rect(
        left = 0f,
        top = 0f,
        right = size.width * 2 / 3,
        bottom = size.height
      )
    )
  }
}