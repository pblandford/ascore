package org.philblandford.ui.insert.items.pause.compose

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.util.*
import org.philblandford.ui.util.fermataIds

@Composable
fun PauseInsert() {
  RowInsert(fermataIds + pauseIds)
}