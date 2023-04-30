package org.philblandford.ui.main.toprow

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.block
import org.philblandford.ui.main.panel.viewmodels.TabSelection
import org.philblandford.ui.main.panel.viewmodels.TabsViewModel
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.compact

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
  val compact = LocalWindowSizeClass.current.compact()
  if (parts.size > 1) {
    Row(
      Modifier.fillMaxHeight()
        .horizontalScroll(rememberScrollState())
        .offset(y = 5.dp), verticalAlignment = Alignment.Bottom
    ) {
      parts.withIndex().forEach { (index, part) ->
        val foreground = if (selected != index) MaterialTheme.colorScheme.onSurface else
          MaterialTheme.colorScheme.surface
        val background = if (selected != index) MaterialTheme.colorScheme.surface else
          MaterialTheme.colorScheme.onSurface
        Box(
          Modifier.fillMaxHeight(if (compact) 1f else 0.8f)
            .background(background).widthIn(block(0.5f))
            .border(2.dp, foreground, RoundedCornerShape(topEnd = 5.dp))
        ) {
          Text(
            if (short) part.short else part.full,
            Modifier
              .padding(5.dp)
              .clickable { select(index) }.align(Alignment.Center),
            maxLines = 1,
            color = foreground,
            fontSize = 15.sp,
          )
        }
      }
    }
  }
}


