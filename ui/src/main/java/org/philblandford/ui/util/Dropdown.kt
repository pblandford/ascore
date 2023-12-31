package org.philblandford.ui.util

import GridSelection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.philblandford.ui.common.block


@Composable
fun ImageGridDropdown(
  images: List<Int>, rows: Int, columns: Int,
  modifier: Modifier = Modifier,
  size: Dp = block(),
  tag: (Int) -> String = { "" },
  border: Boolean = false,
  selected: Int, onSelect: (Int) -> Unit
) {
  val showDropdown = remember { mutableStateOf(false) }

  Box(modifier.padding(5.dp)) {
    SquareButton(
      resource = images[selected],
      size = size,
      border = border,
      tag = "Button ${tag(selected)}",
      onClick = { showDropdown.value = !showDropdown.value })
    if (showDropdown.value) {

      DropdownMenu(
        expanded = showDropdown.value,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        onDismissRequest = { showDropdown.value = false }) {

          GridSelection(
            images = images, rows = rows, border = border,
            columns = columns, size = size, tag = {
              tag(it)
            }
          ) {
            onSelect(it)
            showDropdown.value = false
          }
      }
    }
  }
}