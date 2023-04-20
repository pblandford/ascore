package org.philblandford.ui.edit.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.edit.entities.ButtonActions

@Composable
fun DeleteOnlyEdit() {
    DefaultEdit(1f, listOf(ButtonActions.DELETE))
}