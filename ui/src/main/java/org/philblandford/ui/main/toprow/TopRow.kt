package org.philblandford.ui.main.toprow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.philblandford.ui.R
import org.philblandford.ui.util.SquareButton

@Composable
fun TopRow(modifier: Modifier,
           openDrawer:()->Unit,
           showLayoutOptions:()->Unit,
           fullScreen:()->Unit,
           showConsole:()->Unit) {

    Row(modifier.fillMaxWidth().background(MaterialTheme.colors.surface)
      .border(1.dp, MaterialTheme.colors.onSurface)) {
      SquareButton(R.drawable.settings) { openDrawer() }
      Tabs(Modifier.weight(1f))
      SquareButton(R.drawable.fullscreen) {
        showLayoutOptions()
      }
      SquareButton(R.drawable.mixer) {
        showConsole()
      }
      PlayButton()
    }

}