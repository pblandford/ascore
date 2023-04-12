package org.philblandford.ui.main.inputpage.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.clipboard.compose.ClipboardView
import org.philblandford.ui.main.inputpage.viewmodel.MainPageModel
import org.philblandford.ui.main.inputpage.viewmodel.MainPageSideEffect
import org.philblandford.ui.main.inputpage.viewmodel.MainPageViewModel
import org.philblandford.ui.main.panel.compose.Panel
import org.philblandford.ui.main.toprow.TopRow
import org.philblandford.ui.main.utility.compose.UtilityRow
import org.philblandford.ui.play.compose.Mixer
import org.philblandford.ui.screen.compose.ScreenView
import org.philblandford.ui.screen.compose.ScreenZoom
import org.philblandford.ui.util.DraggableItem
import timber.log.Timber


@Composable
fun MainPageView(openDrawer: () -> Unit, setPopupLayout: (LayoutID) -> Unit) {
  Timber.e("RECO MainPageView")

  VMView(MainPageViewModel::class.java) { state, _, effect ->

    val coroutineScope = rememberCoroutineScope()
    val alertText = remember { mutableStateOf<ErrorDescr?>(null) }

    LaunchedEffect(Unit) {
      coroutineScope.launch {
        effect.collectLatest { effect ->
          when (effect) {
            is MainPageSideEffect.Error -> alertText.value = effect.errorDescr
          }
        }
      }
    }

    alertText.value?.let { errorDescr ->
      AlertDialog(onDismissRequest = { alertText.value = null },
        confirmButton = { Button({ alertText.value = null }) { Text("OK") } },

        text = {
          Text(errorDescr.message)
        },
        title = { Text(errorDescr.headline) })
    }

    Box(Modifier.fillMaxSize()) {
      val showConsole = remember { mutableStateOf(false) }
      val showPanel = remember { mutableStateOf(true) }
      val fullScreen = remember { mutableStateOf(false) }
      val uiController = rememberSystemUiController()

      BackHandler(fullScreen.value) {
        fullScreen.value = false
      }

      uiController.isStatusBarVisible = !fullScreen.value


      if (fullScreen.value) {
        ScreenBox(Modifier.fillMaxSize(), state)
      } else {
        Column(
          Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
        ) {
          TopRow(
            Modifier,
            openDrawer,
            { setPopupLayout(LayoutID.LAYOUT_OPTIONS) },
            { fullScreen.value = true }) {
            showConsole.value = !showConsole.value
          }

          Timber.e("show clip ${state.showClipboard}")


          Box(Modifier.fillMaxWidth()) {

            ScreenBox(Modifier.fillMaxSize(), state)

            Column(Modifier.align(Alignment.BottomCenter)) {
              AnimatedVisibility(showPanel.value, enter = slideInVertically() { it },
                exit = slideOutVertically() { it }
              ) {
                Panel()
              }
              UtilityRow(showPanel.value) { showPanel.value = !showPanel.value }
            }

            if (showConsole.value) {
              Mixer(Modifier.align(Alignment.BottomEnd))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ScreenBox(modifier: Modifier, state: MainPageModel) {
  Timber.e("RECO ScreenBox")


  val clipboardOffset = remember { mutableStateOf(Offset(0f, 10f)) }
  val zoomOffset = remember { mutableStateOf(Offset(-10f, 50f)) }

  Box(modifier) {
    ScreenView()

    if (state.showClipboard) {
      DraggableItem(Modifier.align(Alignment.TopCenter), clipboardOffset) {
        ClipboardView(Modifier)
      }

      state.selectedArea?.let { location ->
        DraggableItem(Modifier.align(Alignment.TopEnd), zoomOffset) {
          ScreenZoom(
            Modifier
              .align(Alignment.TopEnd), location
          )
        }
      }
    }
  }
}