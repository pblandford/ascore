package org.philblandford.ui.insert.items.dynamic.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.insert.row.compose.RowInsertInternal
import org.philblandford.ui.util.UpDownDependent
import org.philblandford.ui.util.dynamicIds

@Composable
fun DynamicInsert() {
  RowInsert(dynamicIds, selected = 2) { model, item, iface ->
    Row {
      RowInsertInternal(model, iface, rows = 2)
      Spacer(Modifier.width(block(0.5)))
      UpDownDependent(
        Modifier.align(Alignment.CenterVertically),
        { item.getParam<Boolean>(EventParam.IS_UP) == true },
        { iface.setParam(EventParam.IS_UP, it) })
    }
  }
}