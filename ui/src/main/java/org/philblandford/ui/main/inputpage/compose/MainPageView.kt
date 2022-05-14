package org.philblandford.ui.main.inputpage.compose

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.clipboard.compose.ClipboardView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.main.inputpage.viewmodel.MainPageViewModel
import org.philblandford.ui.main.panel.compose.Panel
import org.philblandford.ui.main.utility.compose.UtilityRow
import org.philblandford.ui.main.toprow.TopRow
import org.philblandford.ui.play.compose.Mixer
import org.philblandford.ui.screen.compose.ScreenView
import timber.log.Timber


@Composable
fun MainPageView() {
  VMView(MainPageViewModel::class.java) { state, _, _ ->

    Box(Modifier.fillMaxSize()) {
      val showConsole = remember { mutableStateOf(false) }
      val showPanel = remember { mutableStateOf(true) }

      ScreenView()
      Column(Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
        TopRow(Modifier) { showConsole.value = !showConsole.value }
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