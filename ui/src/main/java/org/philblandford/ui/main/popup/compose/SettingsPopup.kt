package org.philblandford.ui.main.popup.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Popup
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.create.compose.CreateScore
import org.philblandford.ui.theme.PopupTheme

@Composable
fun SettingsPopup(layoutID: LayoutID, dismiss:()->Unit) {
  Popup(Alignment.Center, onDismissRequest = dismiss) {
    PopupTheme {
      Box(
        Modifier
          .background(Color.White)
      ) {
        Layout(layoutID)
      }
    }
  }
}

@Composable
private fun Layout(layoutID: LayoutID) {
  when (layoutID) {
    LayoutID.NEW_SCORE -> {
      CreateScore()
    }
    else -> {}
  }
}