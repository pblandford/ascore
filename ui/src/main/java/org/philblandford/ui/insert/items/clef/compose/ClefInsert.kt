package org.philblandford.ui.insert.items.clef.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.compact
import org.philblandford.ui.util.clefIds

@Composable
fun ClefInsert() {
  RowInsert(clefIds, rows = if (LocalWindowSizeClass.current.compact()) 2 else 1)
}