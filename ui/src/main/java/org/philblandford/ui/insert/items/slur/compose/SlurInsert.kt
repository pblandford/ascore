package org.philblandford.ui.insert.items.slur.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.util.UpDownRow

@Composable
fun SlurInsert() {
  InsertVMView<InsertModel, InsertInterface<InsertModel>, DefaultInsertViewModel> { _, item, iface ->
    SlurInsertInternal(item, iface)
  }
}

@Composable
private fun SlurInsertInternal(insertItem: InsertItem, insertInterface: InsertInterface<InsertModel>) {
  UpDownRow(isUp = insertItem.getParam(EventParam.IS_UP) ?: true,
    set = { insertInterface.setParam(EventParam.IS_UP, it) }, Modifier.padding(10.dp))
}
