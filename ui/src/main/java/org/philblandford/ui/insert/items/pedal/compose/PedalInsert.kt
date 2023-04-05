package org.philblandford.ui.insert.items.pedal.compose

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.util.articulationIds
import org.philblandford.ui.util.octaveIds
import org.philblandford.ui.util.pedalIds

@Composable
fun PedalInsert() {
  RowInsert(pedalIds)
}