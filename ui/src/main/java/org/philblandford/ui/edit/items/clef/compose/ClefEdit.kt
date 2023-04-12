package org.philblandford.ui.edit.items.clef.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.philblandford.ui.R
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.clefIds

@Composable
fun ClefEdit() {
    RowEdit(clefIds, actions = listOf(ButtonActions.DELETE), scale = 0f)
}