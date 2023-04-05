package org.philblandford.ui.insert.items.navigation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.NavigationType
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.insert.row.compose.RowInsertInternal
import org.philblandford.ui.insert.row.viewmodel.RowInsertInterface
import org.philblandford.ui.insert.row.viewmodel.RowInsertModel
import org.philblandford.ui.util.IdRow
import org.philblandford.ui.R
import org.philblandford.ui.util.navigationIds

@Composable
fun NavigationInsert() {
  RowInsert(navigationIds) { model, item, iface ->
    NavigationInsertInternal(model, item, iface)
  }
}

@Composable
fun NavigationInsertInternal(model: RowInsertModel<NavigationType>, insertItem: InsertItem,
                     iface:RowInsertInterface<NavigationType>) {
  Row(Modifier.wrapContentWidth()) {
    RowInsertInternal(model, iface)
    Spacer(Modifier.width(block()))

    if (model.ids[model.selected].second == NavigationType.CODA) {
      IdRow(
        ids = listOf(R.drawable.left_arrow to true, R.drawable.right_arrow to false),
        selected = if (insertItem.getParam<Boolean>(EventParam.START) == true) 0 else 1 ,
        onSelect = {
          iface.setParam(EventParam.START, insertItem.getParam<Boolean>(EventParam.START) != true)
        },
        modifier = Modifier.align(Alignment.CenterVertically)
      )
    }
  }
}