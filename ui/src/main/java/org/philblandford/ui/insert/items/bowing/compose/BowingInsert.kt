package org.philblandford.ui.insert.items.bowing.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.util.bowingIds

@Composable
fun BowingInsert() {
  RowInsert(bowingIds)
}