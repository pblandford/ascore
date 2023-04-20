package org.philblandford.ui.edit.items.keysignature.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.R
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.clefIds
import org.philblandford.ui.util.keySignatureIds

@Composable
fun KeySignatureEdit() {
    RowEdit(
        keySignatureIds, actions = listOf(ButtonActions.DELETE), rows = 3,
        typeParam = EventParam.SHARPS,
        scale = 0f
    )
}