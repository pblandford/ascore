package org.philblandford.ui.insert.items.bars.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.util.NumberSelector
import org.philblandford.ui.util.ToggleRow

@Composable
fun BarInsert() {
  InsertVMView<InsertModel, InsertInterface<InsertModel>, DefaultInsertViewModel> { _, insertItem, iface ->
    BarInsertInternal(insertItem, iface)
  }
}


@Composable
private fun BarInsertInternal(insertItem: InsertItem, iface: InsertInterface<InsertModel>) {

  Box(
    Modifier
      .fillMaxWidth()
      .padding(10.dp)) {
    Row(Modifier.align(Alignment.CenterStart)) {
      ToggleRow(ids = listOf(R.drawable.add_bar_left, R.drawable.add_bar_right),
        tag = { if (it == 1) "After" else "Before" },
        selected = if (insertItem.getParam<Boolean>(EventParam.AFTER) == true) 1 else 0,
        onSelect = {
          iface.setParam(EventParam.AFTER, it == 1)
        })
      Spacer(Modifier.width(block()))
      NumberSelector(min = 1, max = 32,
        editable = false,
        num = insertItem.getParam(EventParam.NUMBER) ?: 1,
        setNum = { iface.setParam(EventParam.NUMBER, it) })
    }
  }
}
