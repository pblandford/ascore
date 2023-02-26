package org.philblandford.ui.main.toprow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.philblandford.ui.R
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.ThemeBox

@Composable
fun TopRow(modifier: Modifier, openDrawer:()->Unit,
           fullScreen:()->Unit,
           showConsole:()->Unit) {

    Row(modifier.fillMaxWidth().background(MaterialTheme.colors.surface)) {
      SquareButton(R.drawable.settings) { openDrawer() }
      Tabs(Modifier.weight(1f))
      SquareButton(R.drawable.fullscreen) {
        fullScreen()
      }
      SquareButton(R.drawable.mixer) {
        showConsole()
      }
      PlayButton()
    }

}