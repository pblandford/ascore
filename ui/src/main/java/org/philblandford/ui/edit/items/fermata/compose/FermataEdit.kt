package org.philblandford.ui.edit.items.fermata.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.util.fermataIds
import org.philblandford.ui.util.pauseIds

@Composable
fun FermataEdit(scale: Float) {
  RowEdit(fermataIds, scale, rows = 1)
}