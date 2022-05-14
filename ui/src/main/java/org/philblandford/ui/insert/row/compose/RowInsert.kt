package org.philblandford.ui.insert.row.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.row.viewmodel.RowInsertInterface
import org.philblandford.ui.insert.row.viewmodel.RowInsertModel
import org.philblandford.ui.insert.row.viewmodel.RowInsertViewModel
import org.philblandford.ui.util.IdRow

@Composable
fun <T> RowInsert(rowInsertModel: RowInsertModel<T>) {
  InsertVMView<RowInsertModel<T>, RowInsertInterface<T>,
          RowInsertViewModel<T>>(rowInsertModel) { model, iface ->
    RowInsertInternal(model, iface)
  }
}

@Composable
fun <T> RowInsertInternal(
  insertModel: RowInsertModel<T>, iface: RowInsertInterface<T>,
) {
  Box(
    Modifier
      .height(100.dp)
      .fillMaxWidth()
      .padding(start = 10.dp)
  ) {
    val rows = 1
    IdRow(
      Modifier.align(Alignment.CenterStart),
      insertModel.ids,
      rows = rows,
      selected = insertModel.selected, onSelect = {
        iface.selectItem(it)
      })
  }
}