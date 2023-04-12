package org.philblandford.ui.settings.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.settings.model.SettingsModel
import org.philblandford.ui.settings.viewmodel.SettingsInterface
import org.philblandford.ui.settings.viewmodel.SettingsViewModel
import org.philblandford.ui.theme.DialogTheme

@Composable
fun Settings() {
  VMView(SettingsViewModel::class.java) { model, iface, _ ->
    SettingsInternal(model, iface)
  }
}

@Composable
private fun SettingsInternal(model: SettingsModel, iface: SettingsInterface) {

  var idx by remember { mutableStateOf(0) }

  DialogTheme { modifier ->

    Column(
      modifier
        .fillMaxWidth()
        .background(MaterialTheme.colors.surface)
        .padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      TabRow(idx, backgroundColor = MaterialTheme.colors.surface) {
        Tab(
          idx == 0,
          onClick = { idx = 0 },
        ) {
          Text(
            stringResource(R.string.settings_background_color),
            fontSize = 15.sp,
            color = MaterialTheme.colors.onSurface
          )
        }
        Tab(
          idx == 1,
          onClick = { idx = 1 },
        ) {
          Text(
            stringResource(R.string.settings_foreground_color),
            fontSize = 15.sp,
            color = MaterialTheme.colors.onSurface
          )
        }
      }

      Gap(1f)

      val color = if (idx == 0) model.colors.surface else model.colors.onSurface

      ClassicColorPicker(
        Modifier.size(250.dp),
        color = HsvColor.from(color),
        onColorChanged = { hsv: HsvColor ->
          if (idx == 0) {
            iface.setBackgroundColor(hsv)
          } else {
            iface.setForegroundColor(hsv)
          }
        }
      )
    }
  }
}