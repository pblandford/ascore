package org.philblandford.ui.main.popup.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.common.AscorePopup
import org.philblandford.ui.create.compose.CreateScore
import org.philblandford.ui.load.compose.LoadScore
import org.philblandford.ui.save.compose.SaveScore
import org.philblandford.ui.theme.PopupTheme

@Composable
fun SettingsPopup(layoutID: LayoutID, dismiss:()->Unit) {
  Popup(
    Alignment.Center,
    onDismissRequest = {  },
    properties = PopupProperties(focusable = true)
  ) {
    PopupTheme {

      Box(
        Modifier
          .border(3.dp, MaterialTheme.colors.surface)
          .wrapContentSize()
      ) {
        Box(
          Modifier
            .background(MaterialTheme.colors.surface)
            .padding(3.dp)
            .border(3.dp, MaterialTheme.colors.primary)
            .padding(8.dp)
            .wrapContentSize()
        ) {
          Layout(layoutID, dismiss)
        }
      }
    }
  }
}

@Composable
private fun Layout(layoutID: LayoutID, dismiss: () -> Unit) {
  when (layoutID) {
    LayoutID.NEW_SCORE -> {
      CreateScore(dismiss)
    }
    LayoutID.SAVE_SCORE -> {
      SaveScore(dismiss)
    }
    LayoutID.LOAD_SCORE -> {
      LoadScore(dismiss)
    }
    else -> {}
  }
}