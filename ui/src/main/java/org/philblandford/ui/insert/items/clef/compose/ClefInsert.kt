package org.philblandford.ui.insert.items.clef.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.util.clefIds

@Composable
fun ClefInsert() {
  RowInsert(clefIds, rows = 2)
}