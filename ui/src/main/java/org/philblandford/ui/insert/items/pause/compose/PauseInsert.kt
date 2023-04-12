package org.philblandford.ui.insert.items.pause.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.util.fermataIds
import org.philblandford.ui.util.pauseIds

@Composable
fun PauseInsert() {
  RowInsert(fermataIds + pauseIds)
}