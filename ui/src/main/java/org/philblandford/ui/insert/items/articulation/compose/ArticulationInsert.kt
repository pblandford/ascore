package org.philblandford.ui.insert.items.articulation.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.insert.items.articulation.model.articulationModel
import org.philblandford.ui.insert.row.compose.RowInsert

@Composable
fun ArticulationInsert() {
  RowInsert(articulationModel)
}