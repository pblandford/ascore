package org.philblandford.ui.insert.items.octave.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.WedgeType
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.insert.row.compose.RowInsertInternal
import org.philblandford.ui.insert.row.viewmodel.RowInsertInterface
import org.philblandford.ui.insert.row.viewmodel.RowInsertModel
import org.philblandford.ui.util.UpDownRow
import org.philblandford.ui.util.wedgeIds

@Composable
fun WedgeInsert(){
  RowInsert(wedgeIds) { model, item, iface ->
    WedgeInsertInternal(model, item, iface)
  }
}

@Composable
private fun WedgeInsertInternal(model: RowInsertModel<WedgeType>, insertItem: InsertItem, iface:RowInsertInterface<WedgeType>) {
  Row(Modifier.fillMaxWidth().padding(10.dp)) {
    RowInsertInternal(model, iface, 1)
    Spacer(Modifier.width(block()))
    val up = insertItem.getParam<Boolean>(EventParam.IS_UP) == true
    UpDownRow(up,
      set = {
        iface.setParam(EventParam.IS_UP, !up)
      },
      modifier = Modifier.align(Alignment.CenterVertically)
    )
  }
}