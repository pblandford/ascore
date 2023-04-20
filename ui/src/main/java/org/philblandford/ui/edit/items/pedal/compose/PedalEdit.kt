package org.philblandford.ui.edit.items.pedal.compose

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.util.octaveIds
import org.philblandford.ui.util.pedalIds

@Composable
fun PedalEdit(scale: Float) {
  RowEdit(pedalIds, scale, 1)
}