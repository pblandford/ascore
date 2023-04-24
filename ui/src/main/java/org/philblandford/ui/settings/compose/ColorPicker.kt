package org.philblandford.ui.settings.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap

@Composable
fun ColorPicker(modifier: Modifier, colors:ColorScheme, setBackground:(HsvColor)->Unit,
setForeground:(HsvColor)->Unit) {
    var idx by remember { mutableStateOf(0) }

    Column(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TabRow(idx, containerColor = MaterialTheme.colorScheme.surface) {
            Tab(
                idx == 0,
                onClick = { idx = 0 },
            ) {
                Text(
                    stringResource(R.string.settings_background_color),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Tab(
                idx == 1,
                onClick = { idx = 1 },
            ) {
                Text(
                    stringResource(R.string.settings_foreground_color),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Gap(1f)

        val color = if (idx == 0) colors.surface else colors.onSurface

        ClassicColorPicker(
            Modifier.size(250.dp),
            color = HsvColor.from(color),
            onColorChanged = { hsv: HsvColor ->
                if (idx == 0) {
                    setBackground(hsv)
                } else {
                    setForeground(hsv)
                }
            }
        )
    }
}