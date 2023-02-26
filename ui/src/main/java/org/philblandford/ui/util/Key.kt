package org.philblandford.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.philblandford.kscore.api.Ks
import org.philblandford.ui.common.block

fun selectionToKs(selection: Int): Int {
  return if (selection < 8) {
    selection
  } else {
    -(selection - 7)
  }
}

fun ksToSelection(ks: Int): Int {
  return if (ks >= 0) {
    ks
  } else {
    -(ks - 7)
  }
}

@Composable
fun KeySelector(
  selected: Int?,
  onSelect: (Int) -> Unit,
  rows: Int = 3,
  modifier: Modifier = Modifier
) {
  val clefs = keySignatureIds.toList()
  GridSelection(images = clefs.map { it.first },
    rows = rows, columns = clefs.size / rows,
    tag = { selectionToKs(it).toString() },
    itemBorder = true,
    modifier = modifier,
    selected = { selected?.let { ksToSelection(it) } ?: -1 },
    onSelect = {
      onSelect(selectionToKs(it))
    })
}

@Composable
fun KeySignatureGrid(selected: () -> Ks, onSelect: (Ks) -> Unit, modifier: Modifier = Modifier) {
  ImageGridDropdown(
    images = keySignatureIds.map { it.first },
    rows = 3,
    columns = 5,
    border = true,
    size = block(1.5f),
    modifier = modifier,
    tag = { "Key ${selectionToKs(it)}" },
    selected = { ksToSelection(selected()) },
    onSelect
    = { onSelect(selectionToKs(it)) }
  )
}