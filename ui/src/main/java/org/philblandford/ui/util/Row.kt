package org.philblandford.ui.util

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.philblandford.ascore.android.ui.style.disabledColor
import com.philblandford.ascore.android.ui.style.disabledDark
import org.philblandford.ui.common.block


@Composable
fun <T> IdRow(
  modifier: Modifier = Modifier,
  ids: List<Pair<Int, T>>,
  rows: Int = 1,
  columns: Int = ids.size / rows,
  selected: Int,
  onSelect: (Int) -> Unit
) {
  ToggleRow(ids = ids.map { it.first },
    rows = rows, columns = columns,
    selected = selected,
    onSelect = { it?.let { onSelect(it) } },
    modifier = modifier,
    disabledColor = disabledColor,
    tag = {
      ids[it].second.toString()
    })
}


@Composable
fun ToggleRow(
  ids: List<Int>,
  modifier: Modifier = Modifier,
  rows: Int = 1,
  columns: Int = ids.size / rows,
  spacing: Dp = 0.dp,
  border: Boolean = true,
  tag: (Int) -> String = { "" },
  size: @Composable() (Int) -> Dp = { block() },
  disabledColor: Color = com.philblandford.ascore.android.ui.style.disabledColor,
  selected: Int?,
  onSelect: (Int?) -> Unit
) {

  Box(modifier.border(if (border) 1.dp else 0.dp, Color.White)) {
    Column {
      (0 until rows).forEach { row ->
        Row {
          (0 until columns).forEach { column ->
            val idx = (row * columns) + column
            SquareButton(
              resource = ids[idx],
              size = size(idx),
              selected = false,
              foregroundColor = if (selected == idx) MaterialTheme.colors.onSurface else disabledColor,
              tag = tag(idx),
              border = border,
              modifier = Modifier.align(Alignment.CenterVertically),
              onClick = {
                if (selected == idx) {
                  onSelect(null)
                } else {
                  onSelect(idx)
                }
              })
            Spacer(modifier = Modifier.width(spacing))
          }
        }
      }
    }
  }
}