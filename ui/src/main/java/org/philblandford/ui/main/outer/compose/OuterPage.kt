package org.philblandford.ui.main.outer.compose

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
fun OuterPage(modifier: Modifier, isReconfiguration: Boolean) {

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val popupLayout = remember { mutableStateOf<LayoutID?>(null) }
  val showMixer = remember{ mutableStateOf(false) }

  ModalNavigationDrawer(drawerState = drawerState,
    modifier = modifier,
    drawerContent = {
      DrawerItems(
        drawerItems, { drawerItem ->
          popupLayout.value = drawerItem.layoutID
          scope.launch {
            drawerState.close()
          }
        }) { scope.launch { drawerState.close() } }
    }) {
    MainPageView(isReconfiguration,{ scope.launch { drawerState.open() } }, { popupLayout.value = it }, {
      showMixer.value = !showMixer.value
    }, {})
    popupLayout.value?.let { popup ->
      SettingsDialog(popup) {
        popupLayout.value = null
      }
    }
    if (showMixer.value) {
      SettingsDialog(LayoutID.MIXER) {
        showMixer.value = false
      }
    }
  }
}
