package org.philblandford.ui.insert.items.groupstaves.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.insert.row.viewmodel.RowInsertViewModel
import org.philblandford.ui.util.staveJoinIds

@Composable
fun GroupStavesInsert() {
  RowInsert(staveJoinIds)
}