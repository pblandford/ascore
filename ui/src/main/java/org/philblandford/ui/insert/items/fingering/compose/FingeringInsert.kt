package org.philblandford.ui.insert.items.fingering.compose

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.util.articulationIds
import org.philblandford.ui.util.fingeringIds

@Composable
fun FingeringInsert() {
  RowInsert(fingeringIds)
}