package org.philblandford.ui.main.toprow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.philblandford.ui.R
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.ThemeBox

@Composable
fun TopRow(modifier: Modifier, showConsole:()->Unit) {

  ThemeBox {
    Row(modifier.fillMaxWidth()) {
      SquareButton(R.drawable.settings)
      Tabs(Modifier.weight(1f))
      SquareButton(R.drawable.mixer) {
        showConsole()
      }
      PlayButton()
    }
  }
}