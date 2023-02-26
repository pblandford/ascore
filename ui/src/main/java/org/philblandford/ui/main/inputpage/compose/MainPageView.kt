package org.philblandford.ui.main.inputpage.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import com.github.zsoltk.compose.backpress.LocalBackPressHandler
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.clipboard.compose.ClipboardView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.main.inputpage.viewmodel.MainPageSideEffect
import org.philblandford.ui.main.inputpage.viewmodel.MainPageViewModel
import org.philblandford.ui.main.panel.compose.Panel
import org.philblandford.ui.main.utility.compose.UtilityRow
import org.philblandford.ui.main.toprow.TopRow
import org.philblandford.ui.play.compose.Mixer
import org.philblandford.ui.screen.compose.ScreenView
import timber.log.Timber


@Composable
fun MainPageView(openDrawer: () -> Unit) {
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
        confirmButton = { alertText.value = null },

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

      ScreenView()
      if (!fullScreen.value) {
        Column(
          Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
        ) {
          TopRow(Modifier, openDrawer, { fullScreen.value = true }) {
            showConsole.value = !showConsole.value
          }
          Timber.e("show clip ${state.showClipboard}")
          if (state.showClipboard) {
            Gap(0.2f)
            ClipboardView(Modifier.align(Alignment.CenterHorizontally))
          }
        }
        Column(Modifier.align(Alignment.BottomCenter)) {
          AnimatedVisibility(showPanel.value, enter = slideInVertically() { it },
            exit = slideOutVertically() { it }
          ) {
            Panel()
          }
          UtilityRow(showPanel.value) { showPanel.value = !showPanel.value }
        }

        if (showConsole.value) {
          Popup(
            onDismissRequest = { showConsole.value = false },
            alignment = Alignment.Center
          ) {
            Mixer()
          }
        }
      }
    }
  }
}