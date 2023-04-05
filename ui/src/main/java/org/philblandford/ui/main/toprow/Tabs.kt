package org.philblandford.ui.main.toprow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philblandford.ascore.android.ui.style.unselectedColor
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.main.panel.viewmodels.TabSelection
import org.philblandford.ui.main.panel.viewmodels.TabsViewModel
import org.philblandford.ui.util.DimmableBox

@Composable
fun Tabs(modifier: Modifier) {
  VMView(TabsViewModel::class.java, modifier) { state, iface, _ ->
    TabsInternal(state.short, state.instruments, state.selected, iface::select)

  }
}

@Composable
private fun TabsInternal(
  short: Boolean,
  parts: List<TabSelection>,
  selected: Int,
  select: (Int) -> Unit
) {
  if (parts.size > 1) {
    Row(Modifier.scrollable(rememberScrollState(), Orientation.Horizontal)) {
      parts.withIndex().forEach { (index, part) ->
        Box(
          Modifier
            .border(2.dp, MaterialTheme.colors.onSurface, RoundedCornerShape(topEnd = 5.dp))
        ) {
          DimmableBox(
            selected != index, Modifier.clickable(onClick = { select(index) })
          ) {
            Text(
              if (short) part.short else part.full,
              Modifier
                .padding(5.dp),
              maxLines = 1,
              color = if (selected != index) Color.White else MaterialTheme.colors.onSurface, fontSize = 15.sp,
            )
          }
        }
      }
    }
  }
}


