package org.philblandford.ui.edit.items.glissando.compose

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.util.fingeringIds
import org.philblandford.ui.util.glissandoIds
import org.philblandford.ui.util.octaveIds
import org.philblandford.ui.util.pedalIds

@Composable
fun GlissandoEdit(scale: Float) {
  RowEdit(glissandoIds, scale, 1)
}