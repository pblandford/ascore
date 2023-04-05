package org.philblandford.ui.insert.row.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.philblandford.kscore.engine.types.EventParam
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.ParametersDefinition
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.insert.row.viewmodel.RowInsertInterface
import org.philblandford.ui.insert.row.viewmodel.RowInsertModel
import org.philblandford.ui.insert.row.viewmodel.RowInsertViewModel
import org.philblandford.ui.util.IdRow
import timber.log.Timber

@Composable
fun <T> RowInsert(
  ids: List<Pair<Int, T>>,
  selected: Int = 0,
  rows: Int = 1,
  content:@Composable (RowInsertModel<T>, InsertItem, RowInsertInterface<T>)->Unit = { model, _, iface ->
    RowInsertInternal(model, iface, rows)
  }
) {

  val factory = object : ViewModelProvider.Factory {

    override fun <V : ViewModel> create(modelClass: Class<V>): V {
      @Suppress("UNCHECKED_CAST")
      return RowInsertViewModel(ids, selected) as V
    }
  }

  InsertVMView(
    viewModelFactory = { viewModel<RowInsertViewModel<T>>(factory = factory, key = ids.toString()) }
  ) { model, item, iface ->
    content(model, item, iface)
  }
}

@Composable
fun <T> RowInsertInternal(
  insertModel: RowInsertModel<T>, iface: RowInsertInterface<T>,
  rows: Int = 1,
  modifier: Modifier = Modifier,
  ) {
  Box(
    modifier
      .height(100.dp)
      .padding(start = 10.dp)
  ) {
    IdRow(
      Modifier.align(Alignment.CenterStart),
      insertModel.ids,
      rows = rows,
      selected = insertModel.selected, onSelect = {
        iface.selectItem(it)
      })
  }
}