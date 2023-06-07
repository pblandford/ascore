package org.philblandford.ui.insert.items.beam.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.compact
import org.philblandford.ui.util.beamIds
import org.philblandford.ui.util.clefIds

@Composable
fun BeamInsert() {
  RowInsert(beamIds)
}