package org.philblandford.ui.edit.items.octave.compose

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.util.octaveIds

@Composable
fun OctaveEdit(scale: Float) {
  RowEdit(octaveIds, scale, 1, typeParam = EventParam.NUMBER)
}