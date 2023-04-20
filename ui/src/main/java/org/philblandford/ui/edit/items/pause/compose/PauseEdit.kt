package org.philblandford.ui.edit.items.pause.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.util.fermataIds
import org.philblandford.ui.util.pauseIds

@Composable
fun PauseEdit(scale: Float) {
  RowEdit(pauseIds, scale, rows = 1)
}