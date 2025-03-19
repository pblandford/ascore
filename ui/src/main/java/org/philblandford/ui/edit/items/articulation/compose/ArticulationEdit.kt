package org.philblandford.ui.edit.items.articulation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.philblandford.ui.R
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.articulationIds
import org.philblandford.ui.util.clefIds

@Composable
fun ArticulationEdit() {
    RowEdit(articulationIds, rows = 1, actions = listOf(ButtonActions.DELETE), scale = 0f)
}