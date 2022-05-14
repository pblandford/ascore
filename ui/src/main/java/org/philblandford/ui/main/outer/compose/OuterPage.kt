package org.philblandford.ui.main.outer.compose

import androidx.compose.material.DrawerValue
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalDrawer
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.main.inputpage.compose.MainPageView
import org.philblandford.ui.main.outer.model.drawerItems
import org.philblandford.ui.main.popup.compose.SettingsPopup


@Composable
fun OuterPage() {

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val popupLayout = remember{ mutableStateOf<LayoutID?>(null) }

  LaunchedEffect(drawerState) {
    if (drawerState.isClosed) drawerState.open() else drawerState.close()
  }

  ModalDrawer(drawerState = drawerState,
    drawerShape = MaterialTheme.shapes.small,
    drawerContent = {
      DrawItems(
        drawerItems
      ) { drawerItem ->
        popupLayout.value = drawerItem.layoutID
        scope.launch {
          drawerState.close()
        }
      }
    }) {
    MainPageView()
    popupLayout.value?.let { popup ->
      SettingsPopup(popup) {
        popupLayout.value = null
      }
    }
  }
}