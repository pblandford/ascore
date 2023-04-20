package org.philblandford.ui.edit.items.ornament.compose

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.util.fingeringIds
import org.philblandford.ui.util.octaveIds
import org.philblandford.ui.util.ornamentIds
import org.philblandford.ui.util.pedalIds

@Composable
fun OrnamentEdit() {
  RowEdit(ornamentIds, 1f, 1, actions = listOf(ButtonActions.DELETE))
}