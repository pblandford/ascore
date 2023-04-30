package org.philblandford.ui.edit.items.stavejoin.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.philblandford.ui.R
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.clefIds
import org.philblandford.ui.util.staveJoinIds

@Composable
fun StaveJoinEdit() {
    RowEdit(staveJoinIds, rows = 1, actions = listOf(ButtonActions.DELETE), scale = 0f)
}